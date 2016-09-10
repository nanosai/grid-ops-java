package com.nanosai.gridops.tcp;

import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;

/**
 * Created by jjenkov on 10-09-2016.
 */
public class TCPSocketsProxyTest {

    public void testConstructors(){

    }

    @Test
    public void test() throws IOException {
        BlockingQueue           blockingQueue = new ArrayBlockingQueue(1024);
        IAPMessageReaderFactory messageReaderFactory = new IAPMessageReaderFactory();
        MemoryAllocator readMemoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024], (allocator) -> new TCPMessage(allocator));
        MemoryAllocator writeMemoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024], (allocator) -> new TCPMessage(allocator));

        TCPSocketsProxy proxy = new TCPSocketsProxy(blockingQueue, messageReaderFactory,
                readMemoryAllocator, writeMemoryAllocator);


        MemoryBlock[] dest          = new MemoryBlock[128];
        TCPSocket[]   readySockets  = new TCPSocket[128];
        TCPSocketPool tcpSocketPool = new TCPSocketPool(1024);

        TCPSocketMock tcpSocketMock1 = createTCPSocketMock(tcpSocketPool, readMemoryAllocator);
        tcpSocketMock1.length        =  IapUtil.createMessage(tcpSocketMock1.bytesToRead, 0);
        tcpSocketMock1.length        += IapUtil.createMessage(tcpSocketMock1.bytesToRead, tcpSocketMock1.length);
        tcpSocketMock1.windowSize    = 1;

        readySockets[0] = tcpSocketMock1;

        TCPSocketMock tcpSocketMock2 = createTCPSocketMock(tcpSocketPool, readMemoryAllocator);
        tcpSocketMock2.length        =  IapUtil.createMessage(tcpSocketMock2.bytesToRead, 0);
        tcpSocketMock2.length        += IapUtil.createMessage(tcpSocketMock2.bytesToRead, tcpSocketMock2.length);
        tcpSocketMock2.windowSize    = 1;

        readySockets[1] = tcpSocketMock2;

        int read = proxy.read(dest, readySockets, 1);
        assertEquals(2, read);

        assertEquals(30, tcpSocketMock1.bytesRead);
        assertEquals(31, tcpSocketMock1.doSocketReadCallCount);

        assertEquals(0, tcpSocketMock2.bytesRead);
        assertEquals(0, tcpSocketMock2.doSocketReadCallCount);

        tcpSocketMock1.reset();
        tcpSocketMock1.length = 30; // messages are 15 bytes long each
        tcpSocketMock1.windowSize = 1;

        tcpSocketMock2.reset();
        tcpSocketMock2.length = 30; // messages are 15 bytes long each
        tcpSocketMock2.windowSize = 1;

        assertEquals(0, tcpSocketMock1.bytesRead);
        assertEquals(0, tcpSocketMock1.doSocketReadCallCount);

        assertEquals(0, tcpSocketMock2.bytesRead);
        assertEquals(0, tcpSocketMock2.doSocketReadCallCount);

        read = proxy.read(dest, readySockets, 2);
        assertEquals(4, read);

        assertEquals(30, tcpSocketMock1.bytesRead);
        assertEquals(31, tcpSocketMock1.doSocketReadCallCount);

        assertEquals(30, tcpSocketMock2.bytesRead);
        assertEquals(31, tcpSocketMock2.doSocketReadCallCount);
    }


    public TCPSocketMock createTCPSocketMock(TCPSocketPool tcpSocketPool, MemoryAllocator readMemoryAllocator){
        TCPSocketMock tcpSocketMock = new TCPSocketMock(tcpSocketPool);
        tcpSocketMock.messageReader = new IAPMessageReader();
        tcpSocketMock.messageReader.init(readMemoryAllocator);
        tcpSocketMock.bytesToRead   = new byte[1024];
        tcpSocketMock.bytesRead     = 0;

        return tcpSocketMock;
    }

}
