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
public class NodeReactorMock extends NodeReactor {

    public boolean handleMessageCalled = false;
    public TcpMessage enqueuedTcpMessage = null;

    public boolean callSuperReact = false;

    public void callSuperReact(boolean callSuperReact){
        this.callSuperReact = callSuperReact;
    }

    public NodeReactorMock(byte[] nodeId){
        super(nodeId, new ProtocolReactor[0]);
    }

    public NodeReactorMock(byte[] nodeId, ProtocolReactor... protocolReactors) {
        super(nodeId, protocolReactors);
    }

    @Override
    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageBase, TcpSocketsPort tcpSocketsPort) throws Exception {
        this.handleMessageCalled = true;
        if(callSuperReact){
            super.react(message, reader, messageBase, tcpSocketsPort);
        }
    }

    @Override
    protected void enqueueErrorResponse(TcpMessage message, TcpSocketsPort tcpSocketsPort, TcpMessage tcpMessage) throws IOException {
        this.enqueuedTcpMessage = tcpMessage;
    }
}
