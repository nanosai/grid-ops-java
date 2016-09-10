package com.nanosai.gridops.tcp;

import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

        tcpSocket.bytesToRead = new byte[1024];

        ByteBuffer    buffer       = ByteBuffer.allocate(1024 * 1024);
        MemoryBlock[] msgDest      = new MemoryBlock[1024];
        int           msgDstOffset = 0;

        tcpSocket.length = IapUtil.createMessage(tcpSocket.bytesToRead, 0);
        int msgCount = tcpSocket.readMessages(buffer, msgDest, msgDstOffset);
        assertEquals(0, msgCount);
        assertEquals(1, tcpSocket.doSocketReadCallCount);

        tcpSocket.reset();
        tcpSocket.windowSize = 1; //read message 1 byte at a time from byte array
        tcpSocket.length = 15;
        msgCount = tcpSocket.readMessages(buffer, msgDest, msgDstOffset);
        assertEquals( 1, msgCount);
        assertEquals(16, tcpSocket.doSocketReadCallCount);
        assertNotNull(msgDest[0]);
        assertEquals (0, msgDest[0].startIndex);
        assertEquals (15, msgDest[0].endIndex);


        //same test, now read 2 bytes at a time instead of 1 (tcpSocket.windowSize = 2)

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

        tcpSocket.bytesToRead = new byte[1024];

        ByteBuffer    buffer       = ByteBuffer.allocate(1024 * 1024);
        MemoryBlock[] msgDest      = new MemoryBlock[1024];
        int           msgDstOffset = 0;

        tcpSocket.length = IapUtil.createMessage(tcpSocket.bytesToRead, 0);

        tcpSocket.windowSize = 2;
        tcpSocket.length = 15;
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

        tcpSocket.bytesToRead = new byte[1024];

        ByteBuffer    buffer       = ByteBuffer.allocate(1024 * 1024);
        MemoryBlock[] msgDest      = new MemoryBlock[1024];
        int           msgDstOffset = 0;

        tcpSocket.length = IapUtil.createMessage(tcpSocket.bytesToRead, 0);

        tcpSocket.windowSize = 3;
        tcpSocket.length = 15;
        msgDest[0] = null;

        int msgCount = tcpSocket.readMessages(buffer, msgDest, msgDstOffset);
        assertEquals( 1, msgCount);
        assertEquals( 6, tcpSocket.doSocketReadCallCount);
        assertNotNull(msgDest[0]);
        assertEquals (0, msgDest[0].startIndex);
        assertEquals (15, msgDest[0].endIndex);

    }




}
