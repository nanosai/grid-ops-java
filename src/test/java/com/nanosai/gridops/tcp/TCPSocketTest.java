package com.nanosai.gridops.tcp;

import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Created by jjenkov on 08-09-2016.
 */
public class TCPSocketTest {


    @Test
    public void testInstantiation() {
        TCPSocketPool tcpSocketPool = new TCPSocketPool(10);

        TCPSocket tcpSocket = tcpSocketPool.getTCPSocket();
    }


    @Test
    public void testRead_singleMessage_oneByteAtATime() throws IOException {
        TCPSocketPool tcpSocketPool = new TCPSocketPool(10);
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024], (allocator) -> new TCPMessage(allocator));

        TCPSocketMock tcpSocket = new TCPSocketMock(tcpSocketPool);
        tcpSocket.messageReader = new IAPMessageReader();
        tcpSocket.messageReader.init(memoryAllocator);

        tcpSocket.byteSource = new byte[1024];

        ByteBuffer    buffer       = ByteBuffer.allocate(1024 * 1024);
        MemoryBlock[] msgDest      = new MemoryBlock[1024];
        int           msgDstOffset = 0;

        tcpSocket.sourceLength = IapUtil.createMessage(tcpSocket.byteSource, 0);
        int msgCount = tcpSocket.readMessages(buffer, msgDest, msgDstOffset);
        assertEquals(0, msgCount);
        assertEquals(1, tcpSocket.doSocketReadCallCount);

        tcpSocket.reset();
        tcpSocket.readWindowSize = 1; //read message 1 byte at a time from byte array
        tcpSocket.sourceLength = 15;
        msgCount = tcpSocket.readMessages(buffer, msgDest, msgDstOffset);
        assertEquals( 1, msgCount);
        assertEquals(16, tcpSocket.doSocketReadCallCount);
        assertNotNull(msgDest[0]);
        assertEquals (0, msgDest[0].startIndex);
        assertEquals (15, msgDest[0].endIndex);


        //same test, now read 2 bytes at a time instead of 1 (tcpSocket.readWindowSize = 2)

        //todo generate an IONMessage. Make asserts that it is read correctly.
    }

    @Test
    public void testRead_singleMessage_twoBytesAtATime() throws IOException {
        TCPSocketPool tcpSocketPool = new TCPSocketPool(10);
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024], (allocator) -> new TCPMessage(allocator));

        TCPSocketMock tcpSocket = new TCPSocketMock(tcpSocketPool);
        tcpSocket.messageReader = new IAPMessageReader();
        tcpSocket.messageReader.init(memoryAllocator);

        tcpSocket.byteSource = new byte[1024];

        ByteBuffer    buffer       = ByteBuffer.allocate(1024 * 1024);
        MemoryBlock[] msgDest      = new MemoryBlock[1024];
        int           msgDstOffset = 0;

        tcpSocket.sourceLength = IapUtil.createMessage(tcpSocket.byteSource, 0);

        tcpSocket.readWindowSize = 2;
        tcpSocket.sourceLength = 15;
        msgDest[0] = null;

        int msgCount = tcpSocket.readMessages(buffer, msgDest, msgDstOffset);
        assertEquals( 1, msgCount);
        assertEquals( 9, tcpSocket.doSocketReadCallCount);
        assertNotNull(msgDest[0]);
        assertEquals (0, msgDest[0].startIndex);
        assertEquals (15, msgDest[0].endIndex);
    }


    @Test
    public void testRead_singleMessage_threeBytesAtATime() throws IOException {
        TCPSocketPool tcpSocketPool = new TCPSocketPool(10);
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024], (allocator) -> new TCPMessage(allocator));

        TCPSocketMock tcpSocket = new TCPSocketMock(tcpSocketPool);
        tcpSocket.messageReader = new IAPMessageReader();
        tcpSocket.messageReader.init(memoryAllocator);

        tcpSocket.byteSource = new byte[1024];

        ByteBuffer    buffer       = ByteBuffer.allocate(1024 * 1024);
        MemoryBlock[] msgDest      = new MemoryBlock[1024];
        int           msgDstOffset = 0;

        tcpSocket.sourceLength = IapUtil.createMessage(tcpSocket.byteSource, 0);

        tcpSocket.readWindowSize = 3;
        tcpSocket.sourceLength = 15;
        msgDest[0] = null;

        int msgCount = tcpSocket.readMessages(buffer, msgDest, msgDstOffset);
        assertEquals( 1, msgCount);
        assertEquals( 6, tcpSocket.doSocketReadCallCount);
        assertNotNull(msgDest[0]);
        assertEquals (0, msgDest[0].startIndex);
        assertEquals (15, msgDest[0].endIndex);
    }

    @Test
    public void testWrite_singleMessage() throws IOException {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024], (allocator) -> new TCPMessage(allocator));

        TCPSocketPool tcpSocketPool = new TCPSocketPool(10);

        TCPSocketMock tcpSocket = new TCPSocketMock(tcpSocketPool);
        tcpSocket.writeWindowSize = 10;

        tcpSocket.byteDest = new byte[1024];

        ByteBuffer    buffer       = ByteBuffer.allocate(1024 * 1024);

        TCPMessage tcpMessage = (TCPMessage) memoryAllocator.getMemoryBlock();
        tcpMessage.allocate(1024);

        writeDataToMessage(tcpMessage);

        //todo insert some data into the tcpMessage - write to tcpSocket - and verify that the
        // data was actually written to doSocketWrite().

        tcpSocket.write(buffer, tcpMessage);

        assertEquals( 10, tcpSocket.doSocketWriteCallCount);
        assertEquals(100, tcpSocket.destLength);

        assertFullMessageWritten(tcpSocket, 0);


    }

    private void assertFullMessageWritten(TCPSocketMock tcpSocket, int offset) {
        for(int i=0; i<100; i++){
            assertEquals((byte) i, tcpSocket.byteDest[offset + i]);
        }
    }

    private void writeDataToMessage(TCPMessage tcpMessage) {
        for(int i=0; i < 100; i++){
            tcpMessage.memoryAllocator.data[tcpMessage.startIndex + i] = (byte) i;
        }
        tcpMessage.writeIndex = tcpMessage.startIndex + 100;
    }

    @Test
    public void testWriteEnqueued() throws IOException {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024], (allocator) -> new TCPMessage(allocator));

        TCPSocketPool tcpSocketPool = new TCPSocketPool(10);

        TCPSocketMock tcpSocket = new TCPSocketMock(tcpSocketPool);
        tcpSocket.writeWindowSize = 10;

        assertTrue(tcpSocket.isEmpty());

        tcpSocket.byteDest = new byte[1024];

        ByteBuffer    buffer       = ByteBuffer.allocate(1024 * 1024);
        TCPMessage tcpMessage = (TCPMessage) memoryAllocator.getMemoryBlock();
        tcpMessage.allocate(1024);

        writeDataToMessage(tcpMessage);
        tcpSocket.enqueue(tcpMessage);
        assertFalse(tcpSocket.isEmpty());

        tcpSocket.writeQueued(buffer);
        assertTrue(tcpSocket.isEmpty());

        assertEquals(10, tcpSocket.doSocketWriteCallCount);
        assertEquals(100, tcpSocket.bytesWritten);
        assertFullMessageWritten(tcpSocket, 0);


        tcpSocket.doSocketWriteCallCount = 0;
        tcpSocket.writeQueued(buffer);
        assertEquals(0, tcpSocket.doSocketWriteCallCount);  //no messagaes queued - no writes expected

        //test with 2 enqueued messages
        //tcpMessage was freed after writing, so we need to allocate it again
        tcpMessage = (TCPMessage) memoryAllocator.getMemoryBlock();
        tcpMessage.allocate(1024);
        writeDataToMessage(tcpMessage);
        tcpSocket.enqueue(tcpMessage);

        TCPMessage tcpMessage2 = (TCPMessage) memoryAllocator.getMemoryBlock();
        tcpMessage2.allocate(1024);
        writeDataToMessage(tcpMessage2);
        tcpSocket.enqueue(tcpMessage2);

        tcpSocket.writeQueued(buffer);
        assertFullMessageWritten(tcpSocket, 100);
        assertFullMessageWritten(tcpSocket, 200);


        assertTrue(tcpSocket.isEmpty());
        assertEquals(20, tcpSocket.doSocketWriteCallCount);
        assertEquals(300, tcpSocket.bytesWritten);

        tcpSocket.doSocketWriteCallCount = 0;
        tcpSocket.writeQueued(buffer);
        assertEquals(0, tcpSocket.doSocketWriteCallCount);
        assertEquals(300, tcpSocket.bytesWritten);


        // test with 1 enqueued message, and a write cap which imitates that only part of the message can be
        // written to the underlying socket, and verity that half the message is written, and that the other half
        // can be written later, and the queue emptied.
        tcpMessage = (TCPMessage) memoryAllocator.getMemoryBlock();
        tcpMessage.allocate(1024);
        writeDataToMessage(tcpMessage);

        assertTrue(tcpSocket.isEmpty());
        tcpSocket.enqueue(tcpMessage);
        assertFalse(tcpSocket.isEmpty());

        tcpSocket.doSocketWriteCallCount = 0;
        tcpSocket.writeCap = 50; //50 bytes write cap.
        tcpSocket.bytesWritten = 0;

        tcpSocket.writeQueued(buffer);
        assertFalse(tcpSocket.isEmpty());

        assertEquals( 6, tcpSocket.doSocketWriteCallCount);
        assertEquals(50, tcpSocket.bytesWritten);

        tcpSocket.writeQueued(buffer);
        assertFalse(tcpSocket.isEmpty());
        assertEquals(7, tcpSocket.doSocketWriteCallCount);
        assertEquals(50, tcpSocket.bytesWritten);

        tcpSocket.writeCap = 100;
        tcpSocket.writeQueued(buffer);
        assertTrue(tcpSocket.isEmpty());
        assertEquals(12, tcpSocket.doSocketWriteCallCount);
        assertEquals(100, tcpSocket.bytesWritten);

        assertFullMessageWritten(tcpSocket, 300);

    }



}
