package com.nanosai.gridops.node;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.iap.error.ErrorMessageConstants;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpMessagePort;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jjenkov on 24-09-2016.
 */
public class NodeContainerTest {


    @Test
    public void testReact() throws Exception {
        byte[] systemId = new byte[]{0};
        NodeReactorMock node0 = new NodeReactorMock(systemId);
        node0.callSuperReact(false);
        assertFalse(node0.handleMessageCalled);

        TcpMessagePort tcpMessagePort = GridOps.tcpMessagePortBuilder().build();

        NodeContainerMock nodeContainer = new NodeContainerMock();
        nodeContainer.addNodeReactor(node0);

        byte[] dest = new byte[1024];

        int length = writeMessage(systemId, dest);
        IonReader reader = new IonReader();
        reader.setSource(dest, 0, length);
        reader.nextParse();

        IapMessageBase message = new IapMessageBase();

        message.read(reader);

        nodeContainer.react((MemoryBlock) null, reader, message, tcpMessagePort);
        assertTrue(node0.handleMessageCalled);

        node0.handleMessageCalled = false;
        byte[] unknownNodeId = new byte[]{123};
        writeMessage(unknownNodeId, dest);

        nodeContainer.react((MemoryBlock) null, reader, message, tcpMessagePort);
        assertFalse(node0.handleMessageCalled);

    }

    private int writeMessage(byte[] systemId, byte[] dest) {
        IonWriter writer = new IonWriter();
        writer.setDestination(dest, 0);
        writer.setNestedFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        IapMessageBase messageBase = new IapMessageBase();
        messageBase.setReceiverNodeId(systemId);

        messageBase.writeReceiverNodeId(writer);

        //writer.writeObjectEndPop();
        return writer.index;
    }




    @Test
    public void testUnsupportedProtocol() throws Exception {
        NodeContainerMock nodeContainer = new NodeContainerMock();

        TcpMessagePort tcpMessagePort = GridOps.tcpMessagePortBuilder().build();

        IapMessageBase iapMessageBase = new IapMessageBase();
        iapMessageBase.setReceiverNodeId     (new byte[99]);

        nodeContainer.react((MemoryBlock) null, null, iapMessageBase, tcpMessagePort);

        assertNotNull(nodeContainer.enqueuedTcpMessage);
        assertFalse(nodeContainer.enqueuedTcpMessage.startIndex == nodeContainer.enqueuedTcpMessage.writeIndex);

        byte[] sourceBytes = new byte[1];
        int sourceBytesLength = 0;
        IonReader reader = new IonReader().setSource(nodeContainer.enqueuedTcpMessage);

        reader.nextParse();
        assertEquals(IonFieldTypes.OBJECT, reader.fieldType);

        reader.moveInto();

        //receiver node id
        reader.nextParse();
        assertEquals(IonFieldTypes.KEY_SHORT, reader.fieldType);
        reader.nextParse();
        assertEquals(IonFieldTypes.BYTES, reader.fieldType);
        sourceBytesLength = reader.readBytes(sourceBytes);
        assertEquals(0, sourceBytesLength);

        //semantic protocol id
        assertKeyBytes(sourceBytes, reader, (byte) 0);

        //semantic protocol version
        assertKeyBytes(sourceBytes, reader, (byte) 0);

        //message type
        assertKeyBytes(sourceBytes, reader, (byte) 0);

        //error code
        assertKeyBytes(sourceBytes, reader, ErrorMessageConstants.errorIdUnknownNodeId[0]);

        //error message
        reader.nextParse();
        assertEquals(IonFieldTypes.KEY_SHORT, reader.fieldType);
        reader.nextParse();
        assertEquals(IonFieldTypes.UTF_8_SHORT, reader.fieldType);
        assertEquals("Unknown node id", reader.readUtf8String());

        assertFalse(reader.hasNext());
    }


    private void assertKeyBytes(byte[] sourceBytes, IonReader reader, byte expectedByteValue) {
        int sourceBytesLength;
        reader.nextParse();
        assertEquals(IonFieldTypes.KEY_SHORT, reader.fieldType);
        reader.nextParse();
        assertEquals(IonFieldTypes.BYTES, reader.fieldType);
        sourceBytesLength = reader.readBytes(sourceBytes);
        assertEquals(1, sourceBytesLength);
        assertEquals(expectedByteValue, sourceBytes[0]);
    }

}
