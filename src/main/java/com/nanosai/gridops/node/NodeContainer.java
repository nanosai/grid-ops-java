package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.iap.error.ErrorMessageConstants;
import com.nanosai.gridops.iap.error.ErrorMessageWriter;
import com.nanosai.gridops.iap.error.ErrorResponse;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpMessage;
import com.nanosai.gridops.tcp.TcpSocketsPort;

import java.io.IOException;

/**
 * Created by jjenkov on 22-09-2016.
 */
public class NodeContainer {

    private NodeReactor[] nodeReactors = null;

    private IapMessageBase iapMessageBase = new IapMessageBase();
    private IonWriter      ionWriter      = new IonWriter().setNestedFieldStack(new int[2]);
    private ErrorResponse  errorResponse  = new ErrorResponse();


    public NodeContainer(NodeReactor... nodeReactors) {
        this.nodeReactors = nodeReactors;
    }

    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageFields, TcpSocketsPort tcpSocketsPort) throws Exception {
        if(messageFields.receiverNodeIdLength > 0){
            NodeReactor nodeReactor = findNodeReactor(messageFields);

            if(nodeReactor != null){
                nodeReactor.react(message, reader, messageFields, tcpSocketsPort);
            } else {
                sendErrorResponse((TcpMessage) message, tcpSocketsPort);
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
        for(int i = 0; i< nodeReactors.length; i++){
            if(messageFields.equalsReceiverNodeId( this.nodeReactors[i].nodeId)){
                return nodeReactors[i];
            }
        }
        return null;
    }

    private void sendErrorResponse(TcpMessage message, TcpSocketsPort tcpSocketsPort) throws IOException {
        //no node reactor found, send error message back
        this.iapMessageBase.setSemanticProtocolId     (ErrorMessageConstants.errorCodeSemanticProtocolId);
        this.iapMessageBase.setSemanticProtocolVersion(ErrorMessageConstants.semanticProtocolVersion);
        this.iapMessageBase.setMessageType            (ErrorMessageConstants.errorResponseMessageType);

        TcpMessage tcpMessage = tcpSocketsPort.allocateWriteMemoryBlock(1024);
        ionWriter.setDestination(tcpMessage);

        ionWriter.writeObjectBeginPush(1);
        this.iapMessageBase.write(ionWriter);
        errorResponse.writeErrorId(ionWriter, ErrorMessageConstants.errorIdUnknownNodeId);
        errorResponse.writeErrorMessage(ionWriter, "Unknown node id");
        ionWriter.writeObjectEndPop();

        tcpMessage.writeIndex = ionWriter.index;

        enqueueErrorResponse(message, tcpSocketsPort, tcpMessage);
    }

    protected void enqueueErrorResponse(TcpMessage message, TcpSocketsPort tcpSocketsPort, TcpMessage tcpMessage) throws IOException {
        tcpSocketsPort.writeNowOrEnqueue(message.tcpSocket, tcpMessage);
    }


}
