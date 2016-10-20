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
public class NodeReactorTest {


    @Test
    public void testFindProtocolHandler() {
        ProtocolReactor protocolReactor0 = new ProtocolReactor(new byte[]{0}) {
            @Override
            public void react(IonReader reader, IapMessageFields message, TcpSocketsPort tcpSocketsPort) {
            }
        };

        ProtocolReactor protocolReactor1 = new ProtocolReactor(new byte[]{1}) {
            @Override
            public void react(IonReader reader, IapMessageFields message, TcpSocketsPort tcpSocketsPort) {
            }
        };

        NodeReactor systemHandler = new NodeReactor(new byte[]{0}, protocolReactor0, protocolReactor1);

        assertSame(protocolReactor0, systemHandler.findProtocolReactor(new byte[]{0}, 0, 1));
        assertSame(protocolReactor1, systemHandler.findProtocolReactor(new byte[]{1}, 0, 1));

        assertNull(systemHandler.findProtocolReactor(new byte[]{2}, 0, 1));
    }

    @Test
    public void testReact(){
        ProtocolReactorMock protocolHandlerMock = new ProtocolReactorMock(new byte[]{2});
        assertFalse(protocolHandlerMock.handleMessageCalled);

        TcpSocketsPort tcpSocketsPort = null;
        NodeReactor systemHandler = new NodeReactor(new byte[]{0}, protocolHandlerMock);

        byte[] dest = new byte[128];

        int length = writeMessage(new byte[]{2}, dest);

        IonReader reader = new IonReader();
        reader.setSource(dest, 0, length);
        reader.nextParse();

        IapMessageFields message = new IapMessageFields();
        message.data = dest;
        IapMessageFieldsReader.read(reader, message);

        systemHandler.react(reader, message, tcpSocketsPort);
        assertTrue(protocolHandlerMock.handleMessageCalled);

        length = writeMessage(new byte[]{123}, dest);
        reader.setSource(dest, 0, length);
        reader.nextParse();
        protocolHandlerMock.handleMessageCalled = false;

        systemHandler.react(reader, message, tcpSocketsPort);
        assertFalse(protocolHandlerMock.handleMessageCalled);
    }


    private int writeMessage(byte[] semanticProtocolId, byte[] dest) {
        IonWriter writer = new IonWriter();
        writer.setDestination(dest, 0);
        writer.setComplexFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        IapMessageFieldsWriter.writeSemanticProtocolId(writer, semanticProtocolId);

        //writer.writeKeyShort(new byte[]{IapMessageKeys.SEMANTIC_PROTOCOL_ID});
        //writer.writeBytes(new byte[]{semanticProtocolId});

        //writer.writeObjectEndPop();

        return writer.destIndex;
    }

}
