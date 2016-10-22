package com.nanosai.gridops.examples;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.iap.IapMessageFieldsWriter;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.MemoryBlockBatch;
import com.nanosai.gridops.tcp.TcpMessage;
import com.nanosai.gridops.tcp.TcpSocket;
import com.nanosai.gridops.tcp.TcpSocketsPort;

import java.io.IOException;

/**
 * Created by jjenkov on 03-09-2016.
 */
public class TcpClientExample {

    public static void main(String[] args) throws IOException {

        final TcpSocketsPort socketsProxy = GridOps.tcpSocketsPortBuilder().build();

        TcpSocket tcpSocket = socketsProxy.addSocket("localhost", 1111);

        IonWriter ionWriter = GridOps.ionWriter().setNestedFieldStack(new int[2]);

        MemoryBlockBatch responses = new MemoryBlockBatch(10);

        while(true){
            TcpMessage request = socketsProxy.allocateWriteMemoryBlock(1024);
            generateIAPMessage(request, ionWriter);

            request.tcpSocket = tcpSocket;

            System.out.println("Sending message");
            socketsProxy.enqueue(request);

            socketsProxy.writeToSockets();

            sleep(100);

            //try reading from socketsProxy
            int messagesRead = socketsProxy.read(responses);

            System.out.println("messagesRead = " + messagesRead);
            if(messagesRead > 0){
                System.out.println(responses.blocks[0].lengthWritten());
            }

            sleep(2000);
        }

        //todo convenience method for MemoryBlock's as destination
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void generateIAPMessage(TcpMessage request, IonWriter ionWriter) {
        ionWriter.setDestination(request);

        ionWriter.writeObjectBeginPush(1);

        byte[] receiverNodeId = new byte[]{33};
        IapMessageFieldsWriter.writeReceiverNodeId(ionWriter, receiverNodeId);

        byte[] protocolId = new byte[]{22};
        IapMessageFieldsWriter.writeSemanticProtocolId(ionWriter, protocolId);

        byte[] protocolVersion = new byte[] {0};
        IapMessageFieldsWriter.writeSemanticProtocolVersion(ionWriter, protocolVersion);

        byte[] messageType = new byte[]{11};
        IapMessageFieldsWriter.writeMessageType(ionWriter, messageType);

        ionWriter.writeObjectEndPop();

        request.writeIndex = ionWriter.destIndex;

        System.out.println("length = " + request.lengthWritten());
    }
}
