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
public class ProtocolReactor {

    public byte[] protocolId;
    public byte[] protocolVersion;

    private List<MessageReactor> messageReactors = new ArrayList<>();

    private IapMessageBase   iapMessageBase  = new IapMessageBase();
    private ErrorResponse    errorResponse   = new ErrorResponse();
    private IonWriter        ionWriter       = new IonWriter().setNestedFieldStack(new int[4]);


    public ProtocolReactor(byte[] protocolId, byte[] protocolVersion) {
        this.protocolId = protocolId;
        this.protocolVersion = protocolVersion;
    }

    public ProtocolReactor addMessageReactor(MessageReactor messageReactor){
        this.messageReactors.add(messageReactor);
        return this;
    }

    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageBase, TcpMessagePort tcpMessagePort) throws Exception {
        if(messageBase.messageTypeLength > 0){
            MessageReactor messageReactor = findMessageReactor(messageBase.messageTypeSource, messageBase.messageTypeOffset, messageBase.messageTypeLength);
            if(messageReactor != null){
                //System.out.println("Found MessageReactor");
                messageReactor.react(message, reader, messageBase, tcpMessagePort);
            } else {
                sendErrorResponse((TcpMessage) message, tcpMessagePort);
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
        for(int i = 0, n=messageReactors.size(); i<n; i++){
            if(NodeUtil.equals(messageType, offset, length,
                    messageReactors.get(i).messageType, 0, messageReactors.get(i).messageType.length)){
                return messageReactors.get(i);
            }
        }
        return null;
    }


    protected void sendErrorResponse(TcpMessage message, TcpMessagePort tcpMessagePort) throws IOException {
        //no node reactor found, send error message back
        TcpMessage responseMessage = tcpMessagePort.allocateWriteMemoryBlock(1024);
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

        enqueueErrorResponse(message, tcpMessagePort, responseMessage);
    }

    protected void enqueueErrorResponse(TcpMessage message, TcpMessagePort tcpMessagePort, TcpMessage responseMessage) throws IOException {
        tcpMessagePort.writeNowOrEnqueue(message.tcpSocket, responseMessage);
    }


}
