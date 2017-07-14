package com.nanosai.gridops.node;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.iap.error.ErrorMessageConstants;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.tcp.TcpMessagePort;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jjenkov on 24-09-2016.
 */
public class NodeReactorTest {


    @Test
    public void testReact() throws Exception {
        ProtocolReactorMock protocolHandlerMock = new ProtocolReactorMock(new byte[]{2}, new byte[]{0});
        assertFalse(protocolHandlerMock.handleMessageCalled);

        TcpMessagePort tcpMessagePort = GridOps.tcpMessagePortBuilder().build();
        NodeReactorMock nodeReactor = new NodeReactorMock(new byte[]{0});
        nodeReactor.addProtocolReactor(protocolHandlerMock);

        byte[] dest = new byte[128];

        int length = writeMessage(new byte[]{2}, new byte[]{0}, dest);

        IonReader reader = new IonReader();
        reader.setSource(dest, 0, length);
        reader.nextParse();

        IapMessageBase messageBase = new IapMessageBase();
        messageBase.read(reader);

        nodeReactor.callSuperReact(true);
        nodeReactor.react(null, reader, messageBase, tcpMessagePort);
        assertTrue(protocolHandlerMock.handleMessageCalled);

        length = writeMessage(new byte[]{123}, new byte[]{0}, dest);
        reader.setSource(dest, 0, length);
        reader.nextParse();
        protocolHandlerMock.handleMessageCalled = false;


        nodeReactor.react(null, reader, messageBase, tcpMessagePort);
        assertFalse(protocolHandlerMock.handleMessageCalled);

        length = writeMessage(new byte[]{2}, new byte[]{1}, dest);
        reader.setSource(dest, 0, length);
        reader.nextParse();
        protocolHandlerMock.handleMessageCalled = false;

        nodeReactor.react(null, reader, messageBase, tcpMessagePort);
        assertFalse(protocolHandlerMock.handleMessageCalled);
    }


    private int writeMessage(byte[] semanticProtocolId, byte[] semanticProtocolVersion, byte[] dest) {
        IonWriter writer = new IonWriter();
        writer.setDestination(dest, 0);
        writer.setNestedFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        IapMessageBase messageBase = new IapMessageBase();
        messageBase.setSemanticProtocolId  (semanticProtocolId);
        messageBase.writeSemanticProtocolId(writer);

        messageBase.setSemanticProtocolVersion(semanticProtocolVersion);
        messageBase.writeSemanticProtocolVersion(writer);

        return writer.index;
    }




    @Test
    public void testUnsupportedProtocol() throws Exception {
        NodeReactorMock nodeReactor = new NodeReactorMock(new byte[]{0});
        nodeReactor.callSuperReact(true);

        TcpMessagePort tcpMessagePort = GridOps.tcpMessagePortBuilder().build();

        IapMessageBase iapMessageBase = new IapMessageBase();
        iapMessageBase.setSemanticProtocolId     (new byte[99]);
        iapMessageBase.setSemanticProtocolVersion(new byte[0]);

        nodeReactor.react(null, null, iapMessageBase, tcpMessagePort);

        assertNotNull(nodeReactor.enqueuedTcpMessage);
        assertFalse(nodeReactor.enqueuedTcpMessage.startIndex == nodeReactor.enqueuedTcpMessage.writeIndex);

        byte[] sourceBytes = new byte[1];
        int sourceBytesLength = 0;
        IonReader reader = new IonReader().setSource(nodeReactor.enqueuedTcpMessage);

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
        assertKeyBytes(sourceBytes, reader, ErrorMessageConstants.errorIdUnsupportedProtocol[0]);

        //error message
        reader.nextParse();
        assertEquals(IonFieldTypes.KEY_SHORT, reader.fieldType);
        reader.nextParse();
        assertEquals(IonFieldTypes.UTF_8, reader.fieldType);
        assertEquals("Unsupported protocol", reader.readUtf8String());

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
