package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageBase;
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
    public void testReact(){
        ProtocolReactorMock protocolHandlerMock = new ProtocolReactorMock(new byte[]{2}, new byte[]{0});
        assertFalse(protocolHandlerMock.handleMessageCalled);

        TcpSocketsPort tcpSocketsPort = null;
        NodeReactor systemHandler = new NodeReactor(new byte[]{0}, protocolHandlerMock);

        byte[] dest = new byte[128];

        int length = writeMessage(new byte[]{2}, new byte[]{0}, dest);

        IonReader reader = new IonReader();
        reader.setSource(dest, 0, length);
        reader.nextParse();

        IapMessageBase messageBase = new IapMessageBase();
        messageBase.read(reader);

        systemHandler.react(null, reader, messageBase, tcpSocketsPort);
        assertTrue(protocolHandlerMock.handleMessageCalled);

        length = writeMessage(new byte[]{123}, new byte[]{0}, dest);
        reader.setSource(dest, 0, length);
        reader.nextParse();
        protocolHandlerMock.handleMessageCalled = false;

        systemHandler.react(null, reader, messageBase, tcpSocketsPort);
        assertFalse(protocolHandlerMock.handleMessageCalled);

        length = writeMessage(new byte[]{2}, new byte[]{1}, dest);
        reader.setSource(dest, 0, length);
        reader.nextParse();
        protocolHandlerMock.handleMessageCalled = false;

        systemHandler.react(null, reader, messageBase, tcpSocketsPort);
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

        return writer.destIndex;
    }

}
