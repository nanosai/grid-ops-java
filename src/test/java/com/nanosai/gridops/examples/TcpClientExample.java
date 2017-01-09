package com.nanosai.gridops.examples;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.iap.IapMessageBase;
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

        final TcpSocketsPort socketsPort = GridOps.tcpSocketsPortBuilder().build();

        TcpSocket tcpSocket = socketsPort.addSocket("localhost", 1111);

        IonWriter ionWriter = GridOps.ionWriter().setNestedFieldStack(new int[2]);

        MemoryBlockBatch responses = new MemoryBlockBatch(10);

        while(true){
            TcpMessage request = socketsPort.allocateWriteMemoryBlock(1024);
            generateIAPMessage(request, ionWriter);

            request.tcpSocket = tcpSocket;

            System.out.println("Sending message");
            socketsPort.writeNowOrEnqueue(request);

            socketsPort.writeBlock();

            //sleep(100);

            //try reading from socketsProxy
            int messagesRead = socketsPort.readBlock(responses);

            System.out.println("messagesRead = " + messagesRead);
            if(messagesRead > 0){
                System.out.println(responses.blocks[0].lengthWritten());
            }

            sleep(200);
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

        IapMessageBase messageBase = new IapMessageBase();
        messageBase.setReceiverNodeId         (new byte[]{33});
        messageBase.setSemanticProtocolId     (new byte[]{22});
        messageBase.setSemanticProtocolVersion(new byte[] {0});
        messageBase.setMessageType            (new byte[]{11});

        messageBase.write(ionWriter);

        ionWriter.writeObjectEndPop();

        request.writeIndex = ionWriter.index;

        System.out.println("length = " + request.lengthWritten());
    }
}
