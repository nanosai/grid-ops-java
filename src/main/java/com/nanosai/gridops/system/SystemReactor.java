package com.nanosai.gridops.system;

import com.nanosai.gridops.iap.IapMessageKeys;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 23-09-2016.
 */
public class SystemReactor {

    public byte[] systemId = null;

    private ProtocolReactor[] protocolReactors = null;


    public SystemReactor(byte[] systemId, ProtocolReactor... protocolReactors) {
        this.systemId = systemId;
        this.protocolReactors = protocolReactors;
    }


    public void react(IonReader reader, MemoryBlock message){
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isSemanticProtocolIDKey(reader)){
                reader.nextParse();
                int semanticProtocolId = (int) reader.readInt64();

                ProtocolReactor protocolReactor = findProtocolHandler(semanticProtocolId);

                if(protocolReactor != null){
                    protocolReactor.react(reader, message);
                }
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
    public ProtocolReactor findProtocolHandler(int protocolId){
        for(int i = 0; i< protocolReactors.length; i++){
            if(protocolId == protocolReactors[i].protocolId){
                return protocolReactors[i];
            }
        }
        return null;
    }


    private boolean isSemanticProtocolIDKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.SEMANTIC_PROTOCOL_ID;
    }

    public boolean systemIdEquals(byte[] otherId, int offset, int otherIdLength){
        if(this.systemId.length != otherIdLength){
            return false;
        }
        for(int i=0; i < otherIdLength; i++){
            if(this.systemId[i] != otherId[offset + i]){
                return false;
            }
        }
        return true;
    }
}
