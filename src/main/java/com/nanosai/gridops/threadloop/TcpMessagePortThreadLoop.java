package com.nanosai.gridops.threadloop;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlockBatch;
import com.nanosai.gridops.node.NodeContainer;
import com.nanosai.gridops.tcp.TcpMessagePort;

import java.io.IOException;

/**
 * Created by jjenkov on 17-10-2016.
 */
public class TcpMessagePortThreadLoop implements Runnable {

    private TcpMessagePort tcpMessagePort = null;
    private NodeContainer  nodeContainer  = null;

    private boolean stopped = false;

    public TcpMessagePortThreadLoop(TcpMessagePort tcpMessagePort, NodeContainer nodeContainer) {
        this.tcpMessagePort = tcpMessagePort;
        this.nodeContainer  = nodeContainer;
    }

    public synchronized void stop() {
        this.stopped = true;
    }

    public synchronized boolean isStopped() {
        return this.stopped;
    }

    public void run() {

        MemoryBlockBatch memoryBlockBatch = new MemoryBlockBatch(64, 64);
        IonReader reader = new IonReader();
        IapMessageBase messageBase = new IapMessageBase();

        while(! isStopped() ){

            try {
                this.tcpMessagePort.addSocketsFromSocketQueue();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                this.tcpMessagePort.readNow(memoryBlockBatch);
            } catch(IOException e) {
                e.printStackTrace(); //should an IOException ever escape out here?
            }

            for(int i=0; i<memoryBlockBatch.count; i++){
                reader.setSource(memoryBlockBatch.blocks[i]);

                //move inside ION object to first field inside.
                reader.nextParse();
                reader.moveInto();
                reader.nextParse();

                messageBase.read(reader);

                //todo pass the tcpMessagePort as parameter to the react() method.
                try {
                    this.nodeContainer.react(memoryBlockBatch.blocks[i], reader, messageBase, tcpMessagePort);
                } catch (Exception e) {
                    //todo do something sensible with this exception.
                    e.printStackTrace();
                }
            }
            memoryBlockBatch.clear();


            try{
                this.tcpMessagePort.writeNow();
                this.tcpMessagePort.cleanupSockets();
            } catch (IOException e) {
                e.printStackTrace(); //should an IOException ever escape out here?
            }
        }
    }
}
