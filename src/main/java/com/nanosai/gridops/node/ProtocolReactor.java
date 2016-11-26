package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpSocketsPort;

/**
 * Created by jjenkov on 23-09-2016.
 */
public class ProtocolReactor {

    public byte[] protocolId;
    public byte[] protocolVersion;

    private MessageReactor[] messageReactors = null;


    public ProtocolReactor(byte[] protocolId, byte[] protocolVersion, MessageReactor... messageReactors) {
        this.protocolId = protocolId;
        this.protocolVersion = protocolVersion;
        this.messageReactors = messageReactors;
    }

    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageBase, TcpSocketsPort tcpSocketsPort){
        if(messageBase.messageTypeLength > 0){
            MessageReactor messageReactor = findMessageReactor(messageBase.messageTypeSource, messageBase.messageTypeOffset, messageBase.messageTypeLength);
            if(messageReactor != null){
                messageReactor.react(message, reader, messageBase, tcpSocketsPort);
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
