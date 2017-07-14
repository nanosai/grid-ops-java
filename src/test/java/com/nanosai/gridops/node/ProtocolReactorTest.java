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
public class ProtocolReactorTest {


    @Test
    public void testFindMessageHandler() {

        MessageReactor messageReactor0 = new MessageReactor(new  byte[]{0}) {
            @Override
            public void react(MemoryBlock message, IonReader reader, IapMessageBase messageBase, TcpMessagePort tcpSocketsPort) {
            }
        };

        MessageReactor messageReactor1 = new MessageReactor(new byte[]{1}) {
            @Override
            public void react(MemoryBlock message, IonReader reader, IapMessageBase messageBase, TcpMessagePort tcpSocketsPort) {
            }
        };

        ProtocolReactor protocolReactor = new ProtocolReactor(new byte[]{0}, new byte[]{0});
        protocolReactor.addMessageReactor(messageReactor0).addMessageReactor(messageReactor1);

        assertSame(messageReactor0, protocolReactor.findMessageReactor(new byte[]{0}, 0, 1));
        assertSame(messageReactor1, protocolReactor.findMessageReactor(new byte[]{1}, 0, 1));

        assertNull(protocolReactor.findMessageReactor(new byte[]{2}, 0, 1));
    }

    @Test
    public void testReact() throws Exception {
        byte[] messageType = new byte[]{0};
        MessageReactorMock messageHandlerMock = new MessageReactorMock(messageType);
        assertFalse(messageHandlerMock.handleMessageCalled);

        ProtocolReactorMock protocolReactor = new ProtocolReactorMock(new byte[]{0}, new byte[]{0});
        protocolReactor.addMessageReactor(messageHandlerMock);

        byte[] dest = new byte[128];

        TcpMessagePort tcpMessagePort = GridOps.tcpMessagePortBuilder().build();

        int length = writeMessage(messageType, dest);

        IonReader reader = new IonReader();
        reader.setSource(dest, 0, length);
        reader.nextParse();

        IapMessageBase messageBase = new IapMessageBase();
        messageBase.read(reader);

        protocolReactor.react(null, reader, messageBase, tcpMessagePort);
        assertTrue(messageHandlerMock.handleMessageCalled);

        length = writeMessage(new byte[]{123}, dest);
        reader.setSource(dest, 0, length);
        reader.nextParse();

        messageBase.read(reader);

        messageHandlerMock.handleMessageCalled = false;
        protocolReactor.react(null, reader, messageBase, tcpMessagePort);

        assertFalse(messageHandlerMock.handleMessageCalled);


    }


    private int writeMessage(byte[] messageType, byte[] dest) {
        IonWriter writer = new IonWriter();
        writer.setDestination(dest, 0);
        writer.setNestedFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        IapMessageBase messageBase = new IapMessageBase();
        messageBase.setMessageType(messageType);

        messageBase.writeMessageType(writer);

        //writer.writeKeyShort(new byte[]{IapMessageKeys.MESSAGE_TYPE});
        //writer.writeBytes   (new byte[]{messageType});

        //writer.writeObjectEndPop();

        return writer.index;
    }


    @Test
    public void testUnsupportedMessageType() throws Exception {
        ProtocolReactorMock protocolReactor = new ProtocolReactorMock(new byte[]{0}, new byte[]{0});

        TcpMessagePort tcpMessagePort = GridOps.tcpMessagePortBuilder().build();

        IapMessageBase iapMessageBase = new IapMessageBase();
        iapMessageBase.setMessageType(new byte[99]);

        protocolReactor.react(null, null, iapMessageBase, tcpMessagePort);

        assertNotNull(protocolReactor.enqueuedTcpMessage);
        assertFalse(protocolReactor.enqueuedTcpMessage.startIndex == protocolReactor.enqueuedTcpMessage.writeIndex);

        byte[] sourceBytes = new byte[1];
        int sourceBytesLength = 0;
        IonReader reader = new IonReader().setSource(protocolReactor.enqueuedTcpMessage);

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
        assertKeyBytes(sourceBytes, reader, ErrorMessageConstants.errorIdUnsupportedMessageType[0]);

        //error message
        reader.nextParse();
        assertEquals(IonFieldTypes.KEY_SHORT, reader.fieldType);
        reader.nextParse();
        assertEquals(IonFieldTypes.UTF_8, reader.fieldType);
        assertEquals("Unsupported message type", reader.readUtf8String());

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
