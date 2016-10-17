package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageFields;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.tcp.TcpSocketsPort;

/**
 * Created by jjenkov on 23-09-2016.
 */
public class ProtocolReactor {

    //consider using a byte[] instad - for more complex protocol names than numbers.
    public byte[] protocolId = null;

    private MessageReactor[] messageReactors = null;


    public ProtocolReactor(byte[] protocolId, MessageReactor... messageReactors) {
        this.protocolId = protocolId;
        this.messageReactors = messageReactors;
    }

    public void react(IonReader reader, IapMessageFields message, TcpSocketsPort tcpSocketsPort){
        if(message.messageTypeLength > 0){
            MessageReactor messageReactor = findMessageReactor(message.data, message.messageTypeOffset, message.messageTypeLength);
            if(messageReactor != null){
                messageReactor.react(reader, message, tcpSocketsPort);
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


}
