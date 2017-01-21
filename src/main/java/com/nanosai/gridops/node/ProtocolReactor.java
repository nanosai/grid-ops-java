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
public class ProtocolReactor {

    public byte[] protocolId;
    public byte[] protocolVersion;

    private MessageReactor[] messageReactors = null;
    private IapMessageBase   iapMessageBase  = new IapMessageBase();
    private ErrorResponse    errorResponse   = new ErrorResponse();
    private IonWriter        ionWriter       = new IonWriter().setNestedFieldStack(new int[4]);


    public ProtocolReactor(byte[] protocolId, byte[] protocolVersion, MessageReactor... messageReactors) {
        this.protocolId = protocolId;
        this.protocolVersion = protocolVersion;
        this.messageReactors = messageReactors;
    }

    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageBase, TcpSocketsPort tcpSocketsPort) throws Exception {
        if(messageBase.messageTypeLength > 0){
            MessageReactor messageReactor = findMessageReactor(messageBase.messageTypeSource, messageBase.messageTypeOffset, messageBase.messageTypeLength);
            if(messageReactor != null){
                messageReactor.react(message, reader, messageBase, tcpSocketsPort);
            } else {
                sendErrorResponse((TcpMessage) message, tcpSocketsPort);
            }
        }
    }

    /**
     * Finds the message handler matching the given message type. If no message handler found
     * for the given message type, null is returned.
     *
     * @param messageType The message type to find the message handler for.
     * @return The message handler matching the given message type, or null if no message handler found.
     */
    protected MessageReactor findMessageReactor(byte[] messageType, int offset, int length){
        for(int i = 0; i< messageReactors.length; i++){
            if(NodeUtil.equals(messageType, offset, length,
                    messageReactors[i].messageType, 0, messageReactors[i].messageType.length)){
                return messageReactors[i];
            }
        }
        return null;
    }


    protected void sendErrorResponse(TcpMessage message, TcpSocketsPort tcpSocketsPort) throws IOException {
        //no node reactor found, send error message back
        TcpMessage responseMessage = tcpSocketsPort.allocateWriteMemoryBlock(1024);
        ionWriter.setDestination(responseMessage);
        ionWriter.writeObjectBeginPush(1);

        this.iapMessageBase.setSemanticProtocolId     (ErrorMessageConstants.errorCodeSemanticProtocolId);
        this.iapMessageBase.setSemanticProtocolVersion(ErrorMessageConstants.semanticProtocolVersion);
        this.iapMessageBase.setMessageType            (ErrorMessageConstants.errorResponseMessageType);
        this.iapMessageBase.write(ionWriter);

        errorResponse.writeErrorId(ionWriter, ErrorMessageConstants.errorIdUnsupportedMessageType);
        errorResponse.writeErrorMessage(ionWriter, "Unsupported message type");

        ionWriter.writeObjectEndPop();

        responseMessage.writeIndex = ionWriter.index;

        enqueueErrorResponse(message, tcpSocketsPort, responseMessage);
    }

    protected void enqueueErrorResponse(TcpMessage message, TcpSocketsPort tcpSocketsPort, TcpMessage responseMessage) throws IOException {
        tcpSocketsPort.writeNowOrEnqueue(message.tcpSocket, responseMessage);
    }


}
