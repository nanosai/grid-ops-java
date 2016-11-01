package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageFields;
import com.nanosai.gridops.iap.IapMessageFieldsReader;
import com.nanosai.gridops.iap.IapMessageFieldsWriter;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.tcp.TcpSocketsPort;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jjenkov on 24-09-2016.
 */
public class NodeContainerTest {


    @Test
    public void testReact() {
        byte[] systemId = new byte[]{0};
        NodeReactorMock system0 = new NodeReactorMock(systemId);
        assertFalse(system0.handleMessageCalled);

        TcpSocketsPort tcpSocketsPort = null;

        NodeContainer systemContainer = new NodeContainer(system0);

        byte[] dest = new byte[1024];

        int length = writeMessage(systemId, dest);
        IonReader reader = new IonReader();
        reader.setSource(dest, 0, length);
        reader.nextParse();

        IapMessageFields message = new IapMessageFields();
        message.data = dest;
        IapMessageFieldsReader.read(reader, message);

        systemContainer.react(null, reader, message, tcpSocketsPort);
        assertTrue(system0.handleMessageCalled);

        system0.handleMessageCalled = false;
        byte[] unknownNodeId = new byte[]{123};
        writeMessage(unknownNodeId, dest);

        systemContainer.react(null, reader, message, tcpSocketsPort);
        assertFalse(system0.handleMessageCalled);

    }

    private int writeMessage(byte[] systemId, byte[] dest) {
        IonWriter writer = new IonWriter();
        writer.setDestination(dest, 0);
        writer.setNestedFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        IapMessageFieldsWriter.writeReceiverNodeId(writer, systemId);

        //writer.writeObjectEndPop();
        return writer.destIndex;
    }

}
