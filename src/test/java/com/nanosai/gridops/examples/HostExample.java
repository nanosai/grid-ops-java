package com.nanosai.gridops.examples;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.host.Host;
import com.nanosai.gridops.iap.IapMessageFields;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.node.MessageReactor;
import com.nanosai.gridops.node.NodeContainer;
import com.nanosai.gridops.node.NodeReactor;
import com.nanosai.gridops.node.ProtocolReactor;
import com.nanosai.gridops.tcp.TcpServer;
import com.nanosai.gridops.tcp.TcpSocketsPort;

import java.io.IOException;

/**
 * Created by jjenkov on 20-10-2016.
 */
public class HostExample {

    public static void main(String[] args) throws IOException {

        TcpServer tcpServer = GridOps.tcpServerBuilder().buildAndStart();
        TcpSocketsPort tcpSocketsPort = GridOps.tcpSocketsPortBuilder().tcpServer(tcpServer).build();

        byte[] messageType     = new byte[]{11};
        byte[] protocolId      = new byte[]{22};
        byte[] protocolVersion = new byte[]{ 0};
        byte[] nodeId          = new byte[]{33};

        MessageReactor messageReactor = new MessageReactor(messageType) {
            @Override
            public void react(MemoryBlock message, IonReader ionReader, IapMessageFields iapMessageFields, TcpSocketsPort tcpSocketsPort) {
                System.out.println("Reacting to message");
            }
        };

        ProtocolReactor protocolReactor = GridOps.protocolReactor(protocolId, protocolVersion, messageReactor);
        NodeReactor     nodeReactor     = GridOps.nodeReactor    (nodeId    , protocolReactor);
        NodeContainer   nodeContainer   = GridOps.nodeContainer  (nodeReactor);

        Host host = GridOps.hostBuilder()
                .tcpSocketsPort(tcpSocketsPort)
                .nodeContainer(nodeContainer)
                .build();

        host.run();
    }
}
