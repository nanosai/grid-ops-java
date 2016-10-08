package com.nanosai.gridops.tcp;

import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;

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
public class TcpSocketsPort {

    // **************************
    // socket management related variables.
    // **************************
    private BlockingQueue<SocketChannel> socketQueue    = null;
    private Map<Long, TcpSocket>         socketMap      = new HashMap<>(); //todo replace with faster Long, Object map.
    private List<SocketChannel>          newSocketsTemp = new ArrayList<SocketChannel>();

    private long nextSocketId = 1;



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
    // TCP Socket close oriented variables.
    // ***************************
    private List<TcpSocket> socketsToBeClosed = new ArrayList<>();


    public TcpSocketsPort(BlockingQueue<SocketChannel> socketQueue, IMessageReaderFactory messageReaderFactory,
                          MemoryAllocator inboundMessageAllocator, MemoryAllocator outboundMessageAllocator) throws IOException {
        this.socketQueue          = socketQueue;
        this.messageReaderFactory = messageReaderFactory;
        this.readMemoryAllocator  = inboundMessageAllocator;
        this.writeMemoryAllocator = outboundMessageAllocator;
        init();
    }

    public TcpSocketsPort(BlockingQueue<SocketChannel> socketQueue, IMessageReaderFactory messageReaderFactory) throws IOException {
        this(socketQueue,
             messageReaderFactory,
             new MemoryAllocator(new byte[36 * 1024 * 1024], new long[10240],
                    (allocator) -> new TcpMessage(allocator) )
             ,
             new MemoryAllocator(new byte[36 * 1024 * 1024], new long[10240],
                     (allocator) -> new TcpMessage(allocator) )
             );
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

        //todo pool some of these objects - IapMessageReader etc.
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

        return tcpSocket;
    }

    protected int selectReadReadySockets(TcpSocket[] readyTcpSocket, int limit) throws IOException {
        int readReady = this.readSelector.selectNow();

        int readyIndex = 0;
        if(readReady > 0){
            Iterator<SelectionKey> iterator = this.readSelector.selectedKeys().iterator();
            while(iterator.hasNext() && readyIndex < limit){
                SelectionKey selectionKey = iterator.next();

                if(selectionKey.channel().isOpen()){
                    readyTcpSocket[readyIndex++] = (TcpSocket) selectionKey.attachment();
                }
                iterator.remove();
            }
        }
        return readyIndex;
    }

    public int read(MemoryBlock[] msgDest) throws IOException {
        int ready = selectReadReadySockets(this.readySocketsTemp, this.readySocketsTemp.length);

        return read(msgDest, this.readySocketsTemp, ready);
    }

    protected int read(MemoryBlock[] msgDest, TcpSocket[] readReadySockets, int readReadySocketCount) throws IOException {

        int receivedMessageCount = 0;
        for(int i=0; i<readReadySocketCount; i++){
            TcpSocket tcpSocket = readReadySockets[i];

            receivedMessageCount += tcpSocket.readMessages(this.readBuffer, msgDest, receivedMessageCount);

            if(tcpSocket.endOfStreamReached || tcpSocket.state != 0){
                tcpSocket.readSelectorSelectionKey.cancel();
                tcpSocket.isRegisteredWithReadSelector = false;

                this.socketsToBeClosed.add(tcpSocket);
            }
        }

        return receivedMessageCount;
    }




    /*
     *  Write methods below
     */


    public void writeToSockets() throws IOException {
        // Cancel all sockets which have no more data to write.
        cancelEmptySockets();

        // Register all sockets that *have* data and which are not yet registered.
        //registerNonEmptySockets();

        // Select from the Selector.
        selectAndWrite();
    }


    private void selectAndWrite() throws IOException {
        int writeReady = this.writeSelector.selectNow();

        if(writeReady > 0){
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

                keyIterator.remove();
            }

            selectionKeys.clear();
        }
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

    public void enqueue(TcpMessage tcpMessage) throws IOException {
        enqueue(tcpMessage.tcpSocket, tcpMessage);
    }

    public void enqueue(TcpSocket tcpSocket, TcpMessage message) throws IOException {
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

            //System.out.println("Closing TcpSocket");
            try {
                tcpSocket.closeAndFree();
            } catch (IOException e) {
                System.out.println("Error closing TcpSocket:");
                e.printStackTrace();
            }
        }
        this.socketsToBeClosed.clear();

    }

}
