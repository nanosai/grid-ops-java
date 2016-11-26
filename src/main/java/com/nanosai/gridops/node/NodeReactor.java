package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpSocketsPort;

/**
 * Created by jjenkov on 23-09-2016.
 */
public class NodeReactor {

    public byte[] nodeId = null;

    private ProtocolReactor[] protocolReactors = null;



    public NodeReactor(byte[] nodeId, ProtocolReactor... protocolReactors) {
        this.nodeId = nodeId;
        this.protocolReactors = protocolReactors;
    }


    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageFields, TcpSocketsPort tcpSocketsPort) {
        if(messageFields.semanticProtocolIdLength > 0){

            ProtocolReactor protocolReactor = findProtocolReactor(messageFields);
            if(protocolReactor != null){
                protocolReactor.react(message, reader, messageFields, tcpSocketsPort);
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



}
