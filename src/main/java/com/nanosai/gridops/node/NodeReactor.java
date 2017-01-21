package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.iap.error.ErrorMessageConstants;
import com.nanosai.gridops.iap.error.ErrorResponse;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpMessage;
import com.nanosai.gridops.tcp.TcpSocketsPort;

import java.io.IOException;

/**
 * Created by jjenkov on 23-09-2016.
 */
public class NodeReactor {

    public byte[] nodeId = null;

    private ProtocolReactor[] protocolReactors = null;

    private IapMessageBase iapMessageBase = new IapMessageBase();
    private IonWriter ionWriter  = new IonWriter().setNestedFieldStack(new int[2]);
    private ErrorResponse errorResponse = new ErrorResponse();





    public NodeReactor(byte[] nodeId, ProtocolReactor... protocolReactors) {
        this.nodeId = nodeId;
        this.protocolReactors = protocolReactors;
    }


    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageFields, TcpSocketsPort tcpSocketsPort) throws Exception {
        if(messageFields.semanticProtocolIdLength > 0){

            ProtocolReactor protocolReactor = findProtocolReactor(messageFields);
            if(protocolReactor != null){
                protocolReactor.react(message, reader, messageFields, tcpSocketsPort);
            } else {
                sendErrorResponse((TcpMessage) message, tcpSocketsPort);
            }
        }
    }


    /**
     * Finds the message handler matching the given message type. If no message handler found
     * for the given message type, null is returned.
     *
     * @param messageFields The IapMessageMessageFields containing the semantic protocol id and version to find the protocol reactor for.
     * @return The message handler matching the given message type, or null if no message handler found.
     */
    public ProtocolReactor findProtocolReactor(IapMessageBase messageFields){
        for(int i = 0; i< protocolReactors.length; i++){
            if(messageFields.equalsSemanticProtocolId     ( protocolReactors[i].protocolId) &&
               messageFields.equalsSemanticProtocolVersion( protocolReactors[i].protocolVersion)){
                return protocolReactors[i];
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
        errorResponse.writeErrorId(ionWriter, ErrorMessageConstants.errorIdUnsupportedProtocol);
        errorResponse.writeErrorMessage(ionWriter, "Unsupported protocol");
        ionWriter.writeObjectEndPop();
        tcpMessage.writeIndex = ionWriter.index;

        enqueueErrorResponse(message, tcpSocketsPort, tcpMessage);
    }

    protected void enqueueErrorResponse(TcpMessage message, TcpSocketsPort tcpSocketsPort, TcpMessage tcpMessage) throws IOException {
        tcpSocketsPort.writeNowOrEnqueue(message.tcpSocket, tcpMessage);
    }


}
