package com.nanosai.gridops.node;

import com.nanosai.gridops.tcp.TcpMessage;
import com.nanosai.gridops.tcp.TcpSocketsPort;

import java.io.IOException;

/**
 * Created by jjenkov on 20/01/2017.
 */
public class NodeContainerMock extends NodeContainer {

    public TcpMessage enqueuedTcpMessage = null;

    public NodeContainerMock(NodeReactor... nodeReactors) {
        super(nodeReactors);
    }

    @Override
    protected void enqueueErrorResponse(TcpMessage message, TcpSocketsPort tcpSocketsPort, TcpMessage tcpMessage) throws IOException {
        this.enqueuedTcpMessage = tcpMessage;
    }
}
