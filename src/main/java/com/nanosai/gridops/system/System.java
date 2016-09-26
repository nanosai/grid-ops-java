package com.nanosai.gridops.system;

import com.nanosai.gridops.iap.IapMessage;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 23-09-2016.
 */
public class System {

    public byte[] systemId = null;

    private ProtocolHandler[] protocolHandlers = null;


    public System(byte[] systemId, ProtocolHandler ... protocolHandlers) {
        this.systemId = systemId;
        this.protocolHandlers = protocolHandlers;
    }


    public void handleMessage(IonReader reader, MemoryBlock message){
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isSemanticProtocolIDKey(reader)){
                reader.nextParse();
                int semanticProtocolId = (int) reader.readInt64();

                ProtocolHandler protocolHandler = findProtocolHandler(semanticProtocolId);

                if(protocolHandler != null){
                    protocolHandler.handleMessage(reader, message);
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
    public ProtocolHandler findProtocolHandler(int protocolId){
        for(int i=0; i<protocolHandlers.length; i++){
            if(protocolId == protocolHandlers[i].protocolId){
                return protocolHandlers[i];
            }
        }
        return null;
    }


    private boolean isSemanticProtocolIDKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessage.SEMANTIC_PROTOCOL_ID_KEY_VALUE;
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
