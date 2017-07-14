package com.nanosai.gridops.tcp;

import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlockBatch;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;

/**
 * Created by jjenkov on 10-09-2016.
 */
public class TcpMessagePortTest {

    public void testConstructors(){

    }

    @Test
    public void testRead() throws IOException {
        BlockingQueue           blockingQueue = new ArrayBlockingQueue(1024);
        IapMessageReaderFactory messageReaderFactory = new IapMessageReaderFactory();
        MemoryAllocator readMemoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024], (allocator) -> new TcpMessage(allocator));
        MemoryAllocator writeMemoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024], (allocator) -> new TcpMessage(allocator));

        TcpMessagePort proxy = new TcpMessagePort(blockingQueue, messageReaderFactory,
                readMemoryAllocator, writeMemoryAllocator);


        MemoryBlockBatch dest          = new MemoryBlockBatch(128);
        TcpSocket[]   readySockets  = new TcpSocket[128];
        TcpSocketPool tcpSocketPool = new TcpSocketPool(1024);

        TcpSocketMock tcpSocketMock1 = createTCPSocketMock(tcpSocketPool, readMemoryAllocator);
        tcpSocketMock1.sourceLength =  IapUtil.createMessage(tcpSocketMock1.byteSource, 0);
        tcpSocketMock1.sourceLength += IapUtil.createMessage(tcpSocketMock1.byteSource, tcpSocketMock1.sourceLength);
        tcpSocketMock1.readWindowSize = 1;

        readySockets[0] = tcpSocketMock1;

        TcpSocketMock tcpSocketMock2 = createTCPSocketMock(tcpSocketPool, readMemoryAllocator);
        tcpSocketMock2.sourceLength =  IapUtil.createMessage(tcpSocketMock2.byteSource, 0);
        tcpSocketMock2.sourceLength += IapUtil.createMessage(tcpSocketMock2.byteSource, tcpSocketMock2.sourceLength);
        tcpSocketMock2.readWindowSize = 1;

        readySockets[1] = tcpSocketMock2;

        int read = proxy.read(dest, readySockets, 1);
        assertEquals(2, read);

        assertEquals(30, tcpSocketMock1.bytesRead);
        assertEquals(31, tcpSocketMock1.doSocketReadCallCount);

        assertEquals(0, tcpSocketMock2.bytesRead);
        assertEquals(0, tcpSocketMock2.doSocketReadCallCount);

        tcpSocketMock1.reset();
        tcpSocketMock1.sourceLength = 30; // messages are 15 bytes long each
        tcpSocketMock1.readWindowSize = 1;

        tcpSocketMock2.reset();
        tcpSocketMock2.sourceLength = 30; // messages are 15 bytes long each
        tcpSocketMock2.readWindowSize = 1;

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


    public TcpSocketMock createTCPSocketMock(TcpSocketPool tcpSocketPool, MemoryAllocator readMemoryAllocator){
        TcpSocketMock tcpSocketMock = new TcpSocketMock(tcpSocketPool);
        tcpSocketMock.messageReader = new IapMessageReader();
        tcpSocketMock.messageReader.init(readMemoryAllocator);
        tcpSocketMock.byteSource = new byte[1024];
        tcpSocketMock.bytesRead     = 0;

        return tcpSocketMock;
    }





}
