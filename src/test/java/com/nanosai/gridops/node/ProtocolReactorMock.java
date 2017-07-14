package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpMessage;
import com.nanosai.gridops.tcp.TcpMessagePort;

import java.io.IOException;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class ProtocolReactorMock extends ProtocolReactor {

    public boolean handleMessageCalled = false;

    public TcpMessage enqueuedTcpMessage = null;


    public ProtocolReactorMock(byte[] protocolId, byte[] protocolVersion){
        super(protocolId, protocolVersion);
    }


    @Override
    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageBase, TcpMessagePort tcpMessagePort) throws Exception {
        this.handleMessageCalled = true;
        super.react(message, reader, messageBase, tcpMessagePort);
    }

    @Override
    protected void enqueueErrorResponse(TcpMessage message, TcpMessagePort tcpMessagePort, TcpMessage responseMessage) throws IOException {
        this.enqueuedTcpMessage = responseMessage;
    }
}
