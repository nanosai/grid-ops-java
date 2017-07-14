package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.iap.error.ErrorMessageConstants;
import com.nanosai.gridops.iap.error.ErrorResponse;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpMessage;
import com.nanosai.gridops.tcp.TcpMessagePort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jjenkov on 23-09-2016.
 */
public class NodeReactor {

    public byte[] nodeId = null;

    private List<ProtocolReactor> protocolReactors = new ArrayList<>();

    private IapMessageBase iapMessageBase = new IapMessageBase();
    private IonWriter ionWriter  = new IonWriter().setNestedFieldStack(new int[2]);
    private ErrorResponse errorResponse = new ErrorResponse();


    public NodeReactor(byte[] nodeId) {
        this.nodeId = nodeId;
    }

    public ProtocolReactor addProtocolReactor(byte[] protocolId, byte[] protocolVersion){
        ProtocolReactor protocolReactor = new ProtocolReactor(protocolId, protocolVersion);
        this.protocolReactors.add(protocolReactor);
        return protocolReactor;
    }

    public ProtocolReactor addProtocolReactor(ProtocolReactor protocolReactor){
        this.protocolReactors.add(protocolReactor);
        return protocolReactor;
    }

    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageFields, TcpMessagePort tcpMessagePort) throws Exception {
        if(messageFields.semanticProtocolIdLength > 0){

            ProtocolReactor protocolReactor = findProtocolReactor(messageFields);
            if(protocolReactor != null){
                //System.out.println("Found protocol");
                protocolReactor.react(message, reader, messageFields, tcpMessagePort);
            } else {
                sendErrorResponse((TcpMessage) message, tcpMessagePort);
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
        for(int i = 0, n=protocolReactors.size(); i<n; i++){
            if(messageFields.equalsSemanticProtocolId     ( protocolReactors.get(i).protocolId) &&
               messageFields.equalsSemanticProtocolVersion( protocolReactors.get(i).protocolVersion)){
                return protocolReactors.get(i);
            }
        }
        return null;
    }


    private void sendErrorResponse(TcpMessage message, TcpMessagePort tcpMessagePort) throws IOException {
        //no node reactor found, send error message back
        this.iapMessageBase.setSemanticProtocolId     (ErrorMessageConstants.errorCodeSemanticProtocolId);
        this.iapMessageBase.setSemanticProtocolVersion(ErrorMessageConstants.semanticProtocolVersion);
        this.iapMessageBase.setMessageType            (ErrorMessageConstants.errorResponseMessageType);

        TcpMessage tcpMessage = tcpMessagePort.allocateWriteMemoryBlock(1024);

        ionWriter.setDestination(tcpMessage);
        ionWriter.writeObjectBeginPush(1);
        this.iapMessageBase.write(ionWriter);
        errorResponse.writeErrorId(ionWriter, ErrorMessageConstants.errorIdUnsupportedProtocol);
        errorResponse.writeErrorMessage(ionWriter, "Unsupported protocol");
        ionWriter.writeObjectEndPop();
        tcpMessage.writeIndex = ionWriter.index;

        enqueueErrorResponse(message, tcpMessagePort, tcpMessage);
    }

    protected void enqueueErrorResponse(TcpMessage message, TcpMessagePort tcpMessagePort, TcpMessage tcpMessage) throws IOException {
        tcpMessagePort.writeNowOrEnqueue(message.tcpSocket, tcpMessage);
    }


}
