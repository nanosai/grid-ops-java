package com.nanosai.gridops.tcp;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by jjenkov on 07-09-2016.
 */
public class TCPSocketPoolTest {

    @Test
    public void testGetTCPSocket_andFree() throws IOException {
        TCPSocketPool tcpSocketPool = new TCPSocketPool(10);

        TCPSocket tcpSocket = tcpSocketPool.getTCPSocket();

        assertNotNull(tcpSocket);
        assertEquals(0, tcpSocketPool.freePooledTCPSocketCount());

        tcpSocketPool.free(tcpSocket);
        assertEquals(1, tcpSocketPool.freePooledTCPSocketCount());

        TCPSocket tcpSocket2 = tcpSocketPool.getTCPSocket();

        assertSame(tcpSocket, tcpSocket2);
        assertEquals(0, tcpSocketPool.freePooledTCPSocketCount());

        TCPSocket tcpSocket3 = tcpSocketPool.getTCPSocket();

        assertNotSame(tcpSocket2, tcpSocket3);
        assertEquals(0, tcpSocketPool.freePooledTCPSocketCount());


    }



}
