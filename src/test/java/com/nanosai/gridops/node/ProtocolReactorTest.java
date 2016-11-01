package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageFields;
import com.nanosai.gridops.iap.IapMessageFieldsReader;
import com.nanosai.gridops.iap.IapMessageFieldsWriter;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpSocketsPort;
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
            public void react(MemoryBlock message, IonReader reader, IapMessageFields messageFields, TcpSocketsPort tcpSocketsPort) {
            }
        };

        MessageReactor messageReactor1 = new MessageReactor(new byte[]{1}) {
            @Override
            public void react(MemoryBlock message, IonReader reader, IapMessageFields messageFields, TcpSocketsPort tcpSocketsPort) {
            }
        };

        ProtocolReactor protocolReactor = new ProtocolReactor(new byte[]{0}, new byte[]{0}, messageReactor0, messageReactor1);

        assertSame(messageReactor0, protocolReactor.findMessageReactor(new byte[]{0}, 0, 1));
        assertSame(messageReactor1, protocolReactor.findMessageReactor(new byte[]{1}, 0, 1));

        assertNull(protocolReactor.findMessageReactor(new byte[]{2}, 0, 1));
    }

    @Test
    public void testReact() {
        MessageReactorMock messageHandlerMock = new MessageReactorMock(new byte[]{0});
        assertFalse(messageHandlerMock.handleMessageCalled);

        ProtocolReactor protocolReactor = new ProtocolReactor(new byte[]{0}, new byte[]{0}, messageHandlerMock);

        byte[] dest = new byte[128];

        TcpSocketsPort tcpSocketsPort = null;

        byte[] messageType = new byte[]{0};
        int length = writeMessage(messageType, dest);

        IonReader reader = new IonReader();
        reader.setSource(dest, 0, length);
        reader.nextParse();

        IapMessageFields messageFields = new IapMessageFields();
        messageFields.data = dest;
        IapMessageFieldsReader.read(reader, messageFields);

        protocolReactor.react(null, reader, messageFields, tcpSocketsPort);
        assertTrue(messageHandlerMock.handleMessageCalled);

        length = writeMessage(new byte[]{123}, dest);
        reader.setSource(dest, 0, length);
        reader.nextParse();

        IapMessageFieldsReader.read(reader, messageFields);

        messageHandlerMock.handleMessageCalled = false;
        protocolReactor.react(null, reader, messageFields, tcpSocketsPort);

        assertFalse(messageHandlerMock.handleMessageCalled);


    }


    private int writeMessage(byte[] messageType, byte[] dest) {
        IonWriter writer = new IonWriter();
        writer.setDestination(dest, 0);
        writer.setNestedFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        IapMessageFieldsWriter.writeMessageType(writer, messageType);

        //writer.writeKeyShort(new byte[]{IapMessageKeys.MESSAGE_TYPE});
        //writer.writeBytes   (new byte[]{messageType});

        //writer.writeObjectEndPop();

        return writer.destIndex;
    }

}
