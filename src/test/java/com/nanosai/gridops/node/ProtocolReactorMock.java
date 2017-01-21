package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpMessage;
import com.nanosai.gridops.tcp.TcpSocketsPort;

import java.io.IOException;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class ProtocolReactorMock extends ProtocolReactor {

    public boolean handleMessageCalled = false;

    public TcpMessage enqueuedTcpMessage = null;


    public ProtocolReactorMock(byte[] protocolId, byte[] protocolVersion){
        super(protocolId, protocolVersion, new MessageReactor[0]);
    }

    public ProtocolReactorMock(byte[] protocolId, byte[] protocolVersion, MessageReactor... messageReactors) {
        super(protocolId, protocolVersion, messageReactors);
    }


    @Override
    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageBase, TcpSocketsPort tcpSocketsPort) throws Exception {
        this.handleMessageCalled = true;
        super.react(message, reader, messageBase, tcpSocketsPort);
    }

    @Override
    protected void enqueueErrorResponse(TcpMessage message, TcpSocketsPort tcpSocketsPort, TcpMessage responseMessage) throws IOException {
        this.enqueuedTcpMessage = responseMessage;
    }
}
