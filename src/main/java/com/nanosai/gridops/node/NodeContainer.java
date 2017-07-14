package com.nanosai.gridops.node;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.iap.error.ErrorMessageConstants;
import com.nanosai.gridops.iap.error.ErrorResponse;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.mem.MemoryBlockBatch;
import com.nanosai.gridops.tcp.TcpMessage;
import com.nanosai.gridops.tcp.TcpMessagePort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jjenkov on 22-09-2016.
 */
public class NodeContainer {

    //private NodeReactor[] nodeReactors = null;
    private List<NodeReactor> nodeReactors = new ArrayList<>();

    //read related fields - for reading incoming messages.
    private IonReader      ionReader          = GridOps.ionReader();
    private IapMessageBase readIapMessageBase = new IapMessageBase();



    //write related fields - for writing error messages - not used by NodeReactors´, ProtocolReactors or MessageReactors etc.
    private IapMessageBase writeIapMessageBase = new IapMessageBase();
    private IonWriter      ionWriter           = new IonWriter().setNestedFieldStack(new int[2]);
    private ErrorResponse  errorResponse       = new ErrorResponse();


    public NodeContainer(){
    }

    public NodeReactor addNodeReactor(byte[] nodeId){
        NodeReactor nodeReactor = new NodeReactor(nodeId);
        this.nodeReactors.add(nodeReactor);
        return nodeReactor;
    }

    public NodeReactor addNodeReactor(NodeReactor nodeReactor){
        this.nodeReactors.add(nodeReactor);
        return nodeReactor;
    }

    public void react(MemoryBlockBatch messages, TcpMessagePort tcpMessagePort) throws Exception {
        react(messages, this.ionReader, this.readIapMessageBase, tcpMessagePort);
    }

    public void react(MemoryBlockBatch messages, IonReader ionReader, IapMessageBase messageFields, TcpMessagePort tcpMessagePort) throws Exception {
        for(int i=0; i < messages.count; i++){
            MemoryBlock message = messages.blocks[i];
            //System.out.println("Message length: " + message.lengthWritten());

            ionReader.setSource(message);
            ionReader.nextParse();
            //System.out.println("IonReader length: " + ionReader.sourceLength);
            ionReader.moveInto();
            ionReader.nextParse();
            //System.out.println("IonReader fieldLength: " + ionReader.fieldLength);

            messageFields.read(ionReader);
            //System.out.println("Receiver node id length: " + messageFields.receiverNodeIdLength);

            react(message, ionReader, messageFields, tcpMessagePort);
        }
    }

    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageFields, TcpMessagePort tcpMessagePort) throws Exception {
        if(messageFields.receiverNodeIdLength > 0){
            NodeReactor nodeReactor = findNodeReactor(messageFields);

            if(nodeReactor != null){
                //System.out.println("Found node");
                nodeReactor.react(message, reader, messageFields, tcpMessagePort);
            } else {
                sendErrorResponse((TcpMessage) message, tcpMessagePort);
            }
        }
    }

    /**
     * Finds the message handler matching the given message type. If no message handler found
     * for the given message type, null is returned.
     *
     * @param messageFields The message fields containing the receiver node id to find the node reactor for.
     * @return The node reactor matching the given receiver node id, or null if no node reactor found.
     */
    public NodeReactor findNodeReactor(IapMessageBase messageFields){
        for(int i = 0, n=nodeReactors.size(); i < n; i++){
            if(messageFields.equalsReceiverNodeId( this.nodeReactors.get(i).nodeId)){
                return nodeReactors.get(i);
            }
        }
        return null;
    }

    private void sendErrorResponse(TcpMessage message, TcpMessagePort tcpMessagePort) throws IOException {
        //no node reactor found, send error message back
        this.writeIapMessageBase.setSemanticProtocolId     (ErrorMessageConstants.errorCodeSemanticProtocolId);
        this.writeIapMessageBase.setSemanticProtocolVersion(ErrorMessageConstants.semanticProtocolVersion);
        this.writeIapMessageBase.setMessageType            (ErrorMessageConstants.errorResponseMessageType);

        TcpMessage tcpMessage = tcpMessagePort.allocateWriteMemoryBlock(1024);
        ionWriter.setDestination(tcpMessage);

        ionWriter.writeObjectBeginPush(1);
        this.writeIapMessageBase.write(ionWriter);
        errorResponse.writeErrorId(ionWriter, ErrorMessageConstants.errorIdUnknownNodeId);
        errorResponse.writeErrorMessage(ionWriter, "Unknown node id");
        ionWriter.writeObjectEndPop();

        tcpMessage.writeIndex = ionWriter.index;

        enqueueErrorResponse(message, tcpMessagePort, tcpMessage);
    }

    protected void enqueueErrorResponse(TcpMessage message, TcpMessagePort tcpMessagePort, TcpMessage tcpMessage) throws IOException {
        tcpMessagePort.writeNowOrEnqueue(message.tcpSocket, tcpMessage);
    }


}
