package com.nanosai.gridops.tcp;

import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlockBatch;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jjenkov on 06-05-2016.
 */
public class TcpMessagePort {




    // **************************
    // socket management related variables.
    // **************************
    private BlockingQueue<SocketChannel> socketQueue    = null;
    private Map<Long, TcpSocket>         socketMap      = new HashMap<>(); //todo replace with faster Long, Object map.
    private List<SocketChannel>          newSocketsTemp = new ArrayList<SocketChannel>();

    private long nextSocketId = 1;

    private ISocketManager socketManager = null;



    // **************************
    // Read oriented variables.
    // **************************
    private IMessageReaderFactory messageReaderFactory = null;

    private Selector        readSelector = null;
    private ByteBuffer      readBuffer   = null;

    private TcpSocketPool tcpObjectPool       = new TcpSocketPool(1024);  //todo make size configurable.
    private MemoryAllocator readMemoryAllocator = null;

    private TcpSocket[] readySocketsTemp = new TcpSocket[128]; //todo maybe change later to TCPSocketProxy - if that class is added.



    // ***************************
    // Write oriented variables.
    // ***************************
    private Selector   writeSelector   = null;
    private ByteBuffer writeByteBuffer = null;

    private List<TcpSocket> nonEmptyToEmptySockets = new ArrayList<>();

    private MemoryAllocator writeMemoryAllocator = null;



    // ***************************
    // TCP Socket closeOutputStream oriented variables.
    // ***************************
    private List<TcpSocket> socketsToBeClosed = new ArrayList<>();


    public TcpMessagePort(BlockingQueue<SocketChannel> socketQueue, IMessageReaderFactory messageReaderFactory,
                          MemoryAllocator inboundMessageAllocator, MemoryAllocator outboundMessageAllocator) throws IOException {
        this.socketQueue          = socketQueue;
        this.messageReaderFactory = messageReaderFactory;
        this.readMemoryAllocator  = inboundMessageAllocator;
        this.writeMemoryAllocator = outboundMessageAllocator;
        init();
    }

    public TcpMessagePort(BlockingQueue<SocketChannel> socketQueue, IMessageReaderFactory messageReaderFactory) throws IOException {
        this(socketQueue,
             messageReaderFactory,
             new MemoryAllocator(new byte[36 * 1024 * 1024], new long[10240],
                    (allocator) -> new TcpMessage(allocator) )
             ,
             new MemoryAllocator(new byte[36 * 1024 * 1024], new long[10240],
                     (allocator) -> new TcpMessage(allocator) )
             );
    }


    public void setSocketManager(ISocketManager socketManager) {
        this.socketManager = socketManager;
        this.socketManager.init(this);
    }


    private void init() throws IOException {
        this.readSelector         = Selector.open();
        this.readBuffer           = ByteBuffer.allocate(1024 * 1024);

        this.writeSelector        = Selector.open();
        this.writeByteBuffer      = ByteBuffer.allocate(1024 * 1024);
    }



    public void addSocketsFromSocketQueue() throws IOException {
        socketQueue.drainTo(this.newSocketsTemp);

        for(int i=0; i<this.newSocketsTemp.size(); i++){
            SocketChannel newSocket = this.newSocketsTemp.get(i);

            addSocket(newSocket);
       }

       this.newSocketsTemp.clear();
    }


    public TcpSocket addSocket(String host, int tcpPort) throws IOException {
        return addSocket(new InetSocketAddress(host, tcpPort));
    }

    public TcpSocket addSocket(InetSocketAddress address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(address);
        return addSocket(socketChannel);
    }


    public TcpSocket addSocket(SocketChannel newSocket) throws IOException {

        newSocket.configureBlocking(false);

        //todo pool some of these objects - IapMessageFieldsReader etc.
        TcpSocket tcpSocket     = this.tcpObjectPool.getTCPSocket();
        tcpSocket.socketId      = this.nextSocketId++;
        tcpSocket.socketChannel = newSocket;
        tcpSocket.messageReader = this.messageReaderFactory.createMessageReader();
        tcpSocket.messageReader.init(this.readMemoryAllocator);

        this.socketMap.put(tcpSocket.socketId, tcpSocket);

        SelectionKey key = newSocket.register(readSelector, SelectionKey.OP_READ);
        key.attach(tcpSocket);

        tcpSocket.readSelectorSelectionKey = key;
        tcpSocket.isRegisteredWithReadSelector = true;

        if(this.socketManager != null){
            this.socketManager.socketAdded(tcpSocket);
        }

        return tcpSocket;
    }


    public int readNow(MemoryBlockBatch msgDest) throws IOException {
        int readReady = this.readSelector.selectNow();
        if(readReady > 0) {
            int ready = getReadReadySockets(this.readySocketsTemp, this.readySocketsTemp.length);
            return read(msgDest, this.readySocketsTemp, ready);
        }
        return 0;
    }

    public int readBlock(MemoryBlockBatch msgDest) throws IOException {
        while(msgDest.count == 0){
            int readReady = this.readSelector.select();
            if(readReady > 0) {
                int ready = getReadReadySockets(this.readySocketsTemp, this.readySocketsTemp.length);
                read(msgDest, this.readySocketsTemp, ready);
            }
        }
        return msgDest.count;
    }

    protected int getReadReadySockets(TcpSocket[] readyTcpSocket, int limit) throws IOException {

        int readyIndex = 0;
        Iterator<SelectionKey> iterator = this.readSelector.selectedKeys().iterator();
        while(iterator.hasNext() && readyIndex < limit){
            SelectionKey selectionKey = iterator.next();

            if(selectionKey.channel().isOpen()){
                readyTcpSocket[readyIndex++] = (TcpSocket) selectionKey.attachment();
            }
            iterator.remove();
        }
        return readyIndex;
    }

    protected int read(MemoryBlockBatch msgDest, TcpSocket[] readReadySockets, int readReadySocketCount) throws IOException {

        int receivedMessageCount = 0;
        for(int i=0; i<readReadySocketCount; i++){
            TcpSocket tcpSocket = readReadySockets[i];

            receivedMessageCount += tcpSocket.readMessages(this.readBuffer, msgDest);

            if(tcpSocket.endOfStreamReached || tcpSocket.state != TcpSocket.STATE_OPEN){
                tcpSocket.readSelectorSelectionKey.cancel();
                tcpSocket.isRegisteredWithReadSelector = false;

                System.out.println("closing socket soon (read)");

                this.socketsToBeClosed.add(tcpSocket);
            }
        }

        return receivedMessageCount;
    }




    /*
     *  Write methods below
     */

    public void writeNow() throws IOException {
        // Cancel all sockets which have no more data to write.
        cancelEmptySockets();

        int writeReady = this.writeSelector.selectNow();
        if(writeReady > 0){
            write();
        }

    }


    public void writeBlock() throws IOException {
        int registeredSockets = this.writeSelector.keys().size();

        while(registeredSockets > 0){
            // Cancel all sockets which have no more data to write.
            cancelEmptySockets();

            // Select from the Selector.
            int writeReady = this.writeSelector.select();
            if(writeReady > 0){
                write();
            }
            registeredSockets = this.writeSelector.keys().size();
        }

    }


    private void write() throws IOException {
        Set<SelectionKey> selectionKeys = this.writeSelector.selectedKeys();
        Iterator<SelectionKey> keyIterator   = selectionKeys.iterator();

        while(keyIterator.hasNext()){
            SelectionKey key = keyIterator.next();

            TcpSocket socket = (TcpSocket) key.attachment();

            socket.writeQueued(this.writeByteBuffer);

            if(socket.isEmpty()){
                this.nonEmptyToEmptySockets.add(socket);
                //this.emptyToNonEmptySockets.remove(socket); //necessary?
            }
            if(socket.state != TcpSocket.STATE_OPEN){
                key.cancel();
                socket.isRegisteredWithWriteSelector = false;
                System.out.println("closing socket soon (write)");

                this.socketsToBeClosed.add(socket);
            }

            keyIterator.remove();
        }

        selectionKeys.clear();
    }



    private void cancelEmptySockets() {
        /*
        if(this.nonEmptyToEmptySockets.size() > 0){
            System.out.println("Canceling socket selector registrations: " + this.nonEmptyToEmptySockets.size());
        };
        */

        //todo could this be optimized if a List was used instead of a Set ?
        if(nonEmptyToEmptySockets.size() == 0) return;


        for(int i=0, n=nonEmptyToEmptySockets.size(); i<n; i++){
            TcpSocket tcpSocket = nonEmptyToEmptySockets.get(i);
            if(tcpSocket.isEmpty()){
                SelectionKey key = tcpSocket.socketChannel.keyFor(this.writeSelector);
                if(key != null){
                    key.cancel();  //todo how can key be null?
                }
                tcpSocket.isRegisteredWithWriteSelector = false;
            }
        }

        nonEmptyToEmptySockets.clear();
    }



    public TcpMessage getWriteMemoryBlock() {
        return (TcpMessage) this.writeMemoryAllocator.getMemoryBlock();
    }

    public TcpMessage allocateWriteMemoryBlock(int lengthToAllocate) {
        return (TcpMessage) getWriteMemoryBlock().allocate(lengthToAllocate);
    }

    public TcpSocket getTCPSocket(long socketId) {
        return this.socketMap.get(socketId);
    }

    public void writeNowOrEnqueue(TcpMessage tcpMessage) throws IOException {
        writeNowOrEnqueue(tcpMessage.tcpSocket, tcpMessage);
    }

    public void writeNowOrEnqueue(TcpSocket tcpSocket, TcpMessage message) throws IOException {
        if(tcpSocket.isEmpty()){
            //attempt to write message immediately instead of first queueing up the message.
            tcpSocket.write(this.writeByteBuffer, message);

            if(message.readIndex == message.writeIndex){ //if full message written
                message.free();
            } else {  //else queue remainder of message.
                if(!tcpSocket.isRegisteredWithWriteSelector){
                    tcpSocket.socketChannel.register(this.writeSelector, SelectionKey.OP_WRITE, tcpSocket);
                    tcpSocket.isRegisteredWithWriteSelector = true;
                }

                tcpSocket.enqueue(message);
            }

        } else {
            tcpSocket.enqueue(message);
        }

    }


    public List<TcpSocket> getSocketsToBeClosed() {
        return socketsToBeClosed;
    }

    public void cleanupSockets() {
        for(int i=0, n=this.socketsToBeClosed.size(); i < n; i++){
            TcpSocket tcpSocket = this.socketsToBeClosed.get(i);

            try {
                tcpSocket.closeAndFree();
                if(this.socketManager != null){
                    this.socketManager.socketClosed(tcpSocket);
                }
            } catch (IOException e) {
                System.out.println("Error closing TcpSocket:");
                e.printStackTrace();
            }
        }
        this.socketsToBeClosed.clear();

    }

}
