package com.nanosai.gridops.tcp;

import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jjenkov on 06-05-2016.
 */
public class TCPSocketsProxy {

    // **************************
    // Get new sockets oriented variables.
    // **************************
    private BlockingQueue<SocketChannel> socketQueue    = null;
    private Map<Long, TCPSocket>         socketMap      = new HashMap<>(); //todo replace with faster Long, Object map.
    private List<SocketChannel>          newSocketsTemp = new ArrayList<SocketChannel>();

    private long nextSocketId = 1;



    // **************************
    // Read oriented variables.
    // **************************
    private IMessageReaderFactory messageReaderFactory = null;

    private Selector        readSelector = null;
    private ByteBuffer      readBuffer   = null;

    private TCPSocketPool tcpObjectPool       = new TCPSocketPool(1024);  //todo make size configurable.
    private MemoryAllocator readMemoryAllocator = null;

    private TCPSocket[] readySocketsTemp = new TCPSocket[128]; //todo maybe change later to TCPSocketProxy - if that class is added.



    // ***************************
    // Write oriented variables.
    // ***************************
    private Selector   writeSelector   = null;
    private ByteBuffer writeByteBuffer = null;

    private List<TCPSocket> nonEmptyToEmptySockets = new ArrayList<>();

    private MemoryAllocator writeMemoryAllocator = null;



    // ***************************
    // TCP Socket close oriented variables.
    // ***************************
    private List<TCPSocket> socketsToBeClosed = new ArrayList<>();


    public TCPSocketsProxy(BlockingQueue<SocketChannel> socketQueue, IMessageReaderFactory messageReaderFactory,
                           MemoryAllocator inboundMessageAllocator, MemoryAllocator outboundMessageAllocator) throws IOException {
        this.socketQueue          = socketQueue;
        this.messageReaderFactory = messageReaderFactory;
        this.readMemoryAllocator  = inboundMessageAllocator;
        this.writeMemoryAllocator = outboundMessageAllocator;
        init();
    }

    public TCPSocketsProxy(BlockingQueue<SocketChannel> socketQueue, IMessageReaderFactory messageReaderFactory) throws IOException {
        this(socketQueue,
             messageReaderFactory,
             new MemoryAllocator(new byte[36 * 1024 * 1024], new long[10240],
                    (allocator) -> new TCPMessage(allocator) )
             ,
             new MemoryAllocator(new byte[36 * 1024 * 1024], new long[10240],
                     (allocator) -> new TCPMessage(allocator) )
             );
    }



    private void init() throws IOException {
        this.readSelector         = Selector.open();
        this.readBuffer           = ByteBuffer.allocate(1024 * 1024);

        this.writeSelector        = Selector.open();
        this.writeByteBuffer      = ByteBuffer.allocate(1024 * 1024);
    }



    public void drainSocketQueue() throws IOException {
        socketQueue.drainTo(this.newSocketsTemp);

        for(int i=0; i<this.newSocketsTemp.size(); i++){
            SocketChannel newSocket = this.newSocketsTemp.get(i);

            addInboundSocket(newSocket);
       }

       this.newSocketsTemp.clear();
    }

    public TCPSocket addInboundSocket(SocketChannel newSocket) throws IOException {

        newSocket.configureBlocking(false);

        //todo pool some of these objects - IAPMessageReader etc.
        TCPSocket tcpSocket     = this.tcpObjectPool.getTCPSocket();
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

    public int selectReadReadySockets(TCPSocket[] readyTcpSocket, int limit) throws IOException {
        int readReady = this.readSelector.selectNow();

        int readyIndex = 0;
        if(readReady > 0){
            Iterator<SelectionKey> iterator = this.readSelector.selectedKeys().iterator();
            while(iterator.hasNext() && readyIndex < limit){
                SelectionKey selectionKey = iterator.next();

                if(selectionKey.channel().isOpen()){
                    readyTcpSocket[readyIndex++] = (TCPSocket) selectionKey.attachment();
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

    protected int read(MemoryBlock[] msgDest, TCPSocket[] readReadySockets, int readReadySocketCount) throws IOException {

        int receivedMessageCount = 0;
        for(int i=0; i<readReadySocketCount; i++){
            TCPSocket tcpSocket = readReadySockets[i];

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

                TCPSocket socket = (TCPSocket) key.attachment();

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
            TCPSocket tcpSocket = nonEmptyToEmptySockets.get(i);
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



    public TCPMessage getWriteMemoryBlock() {
        return (TCPMessage) this.writeMemoryAllocator.getMemoryBlock();
    }

    public TCPMessage allocateWriteMemoryBlock(int lengthToAllocate) {
        return (TCPMessage) getWriteMemoryBlock().allocate(lengthToAllocate);
    }

    public TCPSocket getTCPSocket(long socketId) {
        return this.socketMap.get(socketId);
    }

    public void enqueue(TCPMessage tcpMessage) throws IOException {
        enqueue(tcpMessage.tcpSocket, tcpMessage);
    }

    public void enqueue(TCPSocket tcpSocket, TCPMessage message) throws IOException {
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


    public List<TCPSocket> getSocketsToBeClosed() {
        return socketsToBeClosed;
    }

    public void cleanupSockets() {
        for(int i=0, n=this.socketsToBeClosed.size(); i < n; i++){
            TCPSocket tcpSocket = this.socketsToBeClosed.get(i);

            //System.out.println("Closing TCPSocket");
            try {
                tcpSocket.closeAndFree();
            } catch (IOException e) {
                System.out.println("Error closing TCPSocket:");
                e.printStackTrace();
            }
        }
        this.socketsToBeClosed.clear();

    }

}
