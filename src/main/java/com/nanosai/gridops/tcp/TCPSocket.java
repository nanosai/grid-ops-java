package com.nanosai.gridops.tcp;


import com.nanosai.gridops.mem.MemoryBlock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by jjenkov on 27-10-2015.
 */
public class TcpSocket {

    private TcpSocketPool tcpSocketPool = null;

    public SocketChannel    socketChannel = null;
    public long             socketId = 0;
    public SelectionKey     readSelectorSelectionKey = null;
    public boolean          isRegisteredWithReadSelector = false;


    public IMessageReader messageReader = null;

    public boolean          isRegisteredWithWriteSelector = false;
    public boolean          endOfStreamReached = false;
    public int              state = 0;

    /*
       WRITING VARIABLES
     */

    private Queue writeQueue   = new Queue(16);


    public TcpSocket(TcpSocketPool tcpSocketPool) {
        this.tcpSocketPool = tcpSocketPool;
    }


    public int readMessages(ByteBuffer tempBuffer, MemoryBlock[] messageDestination, int messageDestinationOffset) throws IOException {
        if(this.state != 0) {
            return 0; //TcpSocket is in an invalid state - no more messages can be read from it.
        }

        tempBuffer.clear();

        int totalBytesRead = read(tempBuffer);

        int messagesRead = 0;
        if(totalBytesRead > 0){
            tempBuffer.flip();

            //todo if more than messageDestination.length messages are written into messageDestination, this will result in an IndexOutOfBoundsException.
            messagesRead = this.messageReader.read(tempBuffer, messageDestination, messageDestinationOffset);

            if(this.messageReader.state() != 0){
                // continue processing the valid messages received in the for loop below - but by setting state to
                // something other than 0, no more messages can be read from this TCP Socket. It is now invalid and
                // should be closed.
                this.state = this.messageReader.state();
            }

            for(int i=messageDestinationOffset, n = messageDestinationOffset+messagesRead; i < n; i++){
                TcpMessage tcpMessage = (TcpMessage) messageDestination[i];
                tcpMessage.socketId    = this.socketId;
                tcpMessage.tcpSocket   = this;
            }
        }

        return messagesRead;
    }

    public int read(ByteBuffer destinationBuffer) throws IOException {
        int bytesRead = 0;

        try{
            bytesRead = doSocketRead(destinationBuffer);
        } catch(IOException e){
            this.endOfStreamReached = true;
            return -1;
        }

        int totalBytesRead = bytesRead;

        while(bytesRead > 0){
            try{
                bytesRead = doSocketRead(destinationBuffer);
                if(bytesRead > 0){
                    totalBytesRead += bytesRead;
                }
            } catch(IOException e){
                this.endOfStreamReached = true;
                return -1;
            }
        }

        if(bytesRead == -1){
            this.endOfStreamReached = true;
        }

        return totalBytesRead;
    }


    /**
     * A method which can be overwritten in mock classes - to NOT read from a socketChannel, but from e.g. a
     * predefined byte array.
     *
     * @param destinationBuffer
     * @return
     * @throws IOException
     */
    protected int doSocketRead(ByteBuffer destinationBuffer) throws IOException {
        return this.socketChannel.read(destinationBuffer);
    }

    public void enqueue(MemoryBlock memoryBlock) {
        this.writeQueue.put(memoryBlock);
    }


    public boolean isEmpty() {
        return this.writeQueue.available() == 0;
    }


    public boolean write(ByteBuffer byteBuffer, TcpMessage message) throws IOException {
        byteBuffer.clear();

        //todo make some calculations to limit the size of data written to that of the ByteBuffer.capacity()
        byteBuffer.put(message.memoryAllocator.data, message.readIndex, message.writeIndex - message.readIndex);

        byteBuffer.flip();

        int bytesWrittenNow = 0;

        do {
            bytesWrittenNow = doSocketWrite(byteBuffer);
            message.readIndex += bytesWrittenNow;
        } while(bytesWrittenNow > 0 && byteBuffer.hasRemaining());

        return bytesWrittenNow > 0; //can write more? Or was bytesWrittenNow == 0 ?
    }


    public void writeQueued(ByteBuffer byteBuffer) throws IOException {
        TcpMessage messageInProgress = (TcpMessage) this.writeQueue.peek();

        boolean canWriteMoreToSocketNow = true;

        while(canWriteMoreToSocketNow && messageInProgress != null){
             canWriteMoreToSocketNow = write(byteBuffer, messageInProgress);

            if(messageInProgress.readIndex == messageInProgress.writeIndex){ //if message fully written to socket...
                this.writeQueue.take();     // remove this message from queue
                messageInProgress.free();   // free the memory allocated to this message

                messageInProgress = (TcpMessage) this.writeQueue.peek();  // take next message in queue, if any.
            }
        }
    }



    public int doSocketWrite(ByteBuffer byteBuffer) throws IOException{
        int bytesWritten      = this.socketChannel.write(byteBuffer);
        int totalBytesWritten = bytesWritten;

        while(bytesWritten > 0 && byteBuffer.hasRemaining()){
            bytesWritten = this.socketChannel.write(byteBuffer);
            totalBytesWritten += bytesWritten;
        }

        return totalBytesWritten;
    }


    /**
     * Closes the TcpSocket + underlying SocketChannel, and frees up all queued inbound and outbound
     * messages.
     *
     * todo maybe split into three methods: close() + free() + closeAndFree()
     */
    public void closeAndFree() throws IOException {
        if(this.messageReader != null){
            this.messageReader.dispose();
            this.messageReader = null;
        }

        if(this.writeQueue != null){
            while(this.writeQueue.available() > 0){
                TcpMessage queuedOutboundMessage = (TcpMessage) writeQueue.take();
                queuedOutboundMessage.free();
            }
        }

        if(this.readSelectorSelectionKey != null){
            this.readSelectorSelectionKey.attach(null);
            if(this.isRegisteredWithReadSelector){
                this.readSelectorSelectionKey.cancel();
            }
            this.readSelectorSelectionKey = null;
        }

        if(this.socketChannel != null){
            this.socketChannel.close();
            this.socketChannel = null;
        }
    }


}
