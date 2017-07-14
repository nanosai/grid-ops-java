package com.nanosai.gridops.threadloop;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlockBatch;
import com.nanosai.gridops.node.NodeContainer;
import com.nanosai.gridops.tcp.TcpMessagePort;

import java.io.IOException;

/**
 * Created by jjenkov on 23/05/2017.
 */
public class ServerThreadLoopActor implements IThreadLoopActor {

    private TcpMessagePort   tcpMessagePort   = null;
    private MemoryBlockBatch memoryBlockBatch = null;
    private NodeContainer    nodeContainer    = null;

    private IonReader        ionReader        = GridOps.ionReader();

    public ServerThreadLoopActor(TcpMessagePort tcpMessagePort, MemoryBlockBatch memoryBlockBatch, NodeContainer nodeContainer) {
        this.tcpMessagePort = tcpMessagePort;
        this.memoryBlockBatch = memoryBlockBatch;
        this.nodeContainer = nodeContainer;
    }

    @Override
    public int act() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("ServerThreadLoopActor Acting");
        try {
            this.tcpMessagePort.addSocketsFromSocketQueue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int actions = 0;
        this.memoryBlockBatch.clear();
        try {
            this.tcpMessagePort.readNow(this.memoryBlockBatch);
            actions = this.memoryBlockBatch.count;

            System.out.println("Received " + this.memoryBlockBatch.count + " messages");

            this.nodeContainer.react(this.memoryBlockBatch, tcpMessagePort);

            this.tcpMessagePort.writeNow();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.tcpMessagePort.cleanupSockets();

        return actions;
    }
}
