package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessage;
import com.nanosai.gridops.ion.read.IonReader;

/**
 * Created by jjenkov on 23-09-2016.
 */
public class NodeReactor {

    public byte[] systemId = null;

    private ProtocolReactor[] protocolReactors = null;


    public NodeReactor(byte[] systemId, ProtocolReactor... protocolReactors) {
        this.systemId = systemId;
        this.protocolReactors = protocolReactors;
    }


    public void react(IonReader reader, IapMessage message) {
        if(message.semanticProtocolIdLength > 0){
            ProtocolReactor protocolReactor =
                    findProtocolReactor(message.data, message.semanticProtocolIdOffset, message.semanticProtocolIdLength);

            if(protocolReactor != null){
                protocolReactor.react(reader, message);
            }
        }
    }


    /**
     * Finds the message handler matching the given message type. If no message handler found
     * for the given message type, null is returned.
     *
     * @param protocolId The message type to find the message handler for.
     * @return The message handler matching the given message type, or null if no message handler found.
     */
    public ProtocolReactor findProtocolReactor(byte[] protocolId, int offset, int length){
        for(int i = 0; i< protocolReactors.length; i++){
            if(NodeUtil.equals(protocolId, offset, length,
                    protocolReactors[i].protocolId, 0, protocolReactors[i].protocolId.length)){
                return protocolReactors[i];
            }
        }
        return null;
    }

}
