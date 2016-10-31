package com.nanosai.gridops.host;

import com.nanosai.gridops.iap.IapMessageFields;
import com.nanosai.gridops.iap.IapMessageFieldsReader;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlockBatch;
import com.nanosai.gridops.node.NodeContainer;
import com.nanosai.gridops.tcp.TcpSocketsPort;

import java.io.IOException;

/**
 * Created by jjenkov on 17-10-2016.
 */
public class Host implements Runnable {

    private TcpSocketsPort tcpSocketsPort = null;
    private NodeContainer  nodeContainer  = null;

    private boolean stopped = false;

    public Host(TcpSocketsPort tcpSocketsPort, NodeContainer nodeContainer) {
        this.tcpSocketsPort = tcpSocketsPort;
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
        IapMessageFields messageFields = new IapMessageFields();

        while(! isStopped() ){

            try {
                this.tcpSocketsPort.addSocketsFromSocketQueue();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                this.tcpSocketsPort.readNow(memoryBlockBatch);
            } catch(IOException e) {
                e.printStackTrace(); //should an IOException ever escape out here?
            }

            for(int i=0; i<memoryBlockBatch.count; i++){
                reader.setSource(memoryBlockBatch.blocks[i]);

                //move inside ION object to first field inside.
                reader.nextParse();
                reader.moveInto();
                reader.nextParse();

                IapMessageFieldsReader.read(reader, messageFields);

                //todo pass the tcpSocketsPort as parameter to the react() method.
                this.nodeContainer.react(reader, messageFields, tcpSocketsPort);
            }
            memoryBlockBatch.clear();


            try{
                this.tcpSocketsPort.writeNow();
                this.tcpSocketsPort.cleanupSockets();
            } catch (IOException e) {
                e.printStackTrace(); //should an IOException ever escape out here?
            }
        }
    }
}