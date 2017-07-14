package com.nanosai.gridops.node;

import com.nanosai.gridops.tcp.TcpMessage;
import com.nanosai.gridops.tcp.TcpMessagePort;

import java.io.IOException;

/**
 * Created by jjenkov on 20/01/2017.
 */
public class NodeContainerMock extends NodeContainer {

    public TcpMessage enqueuedTcpMessage = null;

    public NodeContainerMock() {
        super();
    }

    @Override
    protected void enqueueErrorResponse(TcpMessage message, TcpMessagePort tcpMessagePort, TcpMessage tcpMessage) throws IOException {
        this.enqueuedTcpMessage = tcpMessage;
    }
}
