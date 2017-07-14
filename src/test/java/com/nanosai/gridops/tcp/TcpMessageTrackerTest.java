package com.nanosai.gridops.tcp;

import com.nanosai.gridops.GridOps;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by jjenkov on 22/01/2017.
 */
public class TcpMessageTrackerTest {

    @Test
    public void testInsertFindRemove() throws IOException {
        TcpMessageTracker tcpMessageTracker = new TcpMessageTracker(8);

        TcpMessagePort tcpMessagePort = GridOps.tcpMessagePortBuilder().build();

        TcpMessage tcpMessage1 = tcpMessagePort.allocateWriteMemoryBlock(128);
        tcpMessageTracker.insertMessage(tcpMessage1, 1, 11);
        assertEquals(1, tcpMessageTracker.nextMessageIndex);
        assertEquals(0, tcpMessageTracker.findMessage(1));

        TcpMessage tcpMessage2 = tcpMessagePort.allocateWriteMemoryBlock(128);
        tcpMessageTracker.insertMessage(tcpMessage2, 2, 22);
        assertEquals(2, tcpMessageTracker.nextMessageIndex);
        assertEquals(1, tcpMessageTracker.findMessage(2));

        tcpMessageTracker.removeMessage(1);
        assertEquals(1, tcpMessageTracker.nextMessageIndex);
        assertEquals(0, tcpMessageTracker.findMessage(2));

    }
}
