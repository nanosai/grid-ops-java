package com.nanosai.gridops.system;

import com.nanosai.gridops.iap.IapMessageKeys;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 22-09-2016.
 */
public class SystemContainer {

    private SystemHandler[] systemHandlers = null;

    private IonReader reader         = new IonReader();

    public SystemContainer(SystemHandler... systemHandlers) {
        this.systemHandlers = systemHandlers;
    }

    public void handleMessage(MemoryBlock message){
        reader.setSource(message.memoryAllocator.data, message.startIndex, message.writeIndex);

        reader.nextParse(); //go to first ION field in message.

        if(reader.fieldType != IonFieldTypes.OBJECT){
            return; //todo do some error handling logic - only ION Object fields are allowed in here.
        }

        reader.moveInto();
        reader.nextParse();

        if(reader.fieldType != IonFieldTypes.KEY_SHORT){
            //todo This is not a KEY_SHORT field - not a System ID key field
            return;
        }
        if(!isSystemIDKey()){
            //todo use default system to handle the message?
            return;
        }

        reader.nextParse();
        if(reader.fieldType == IonFieldTypes.BYTES){

            SystemHandler systemHandler = findSystem(reader.source, reader.index, reader.fieldLength);

            if(systemHandler != null){
                reader.nextParse();
                systemHandler.handleMessage(reader, message);
                return;
            }
        }
    }

    /**
     * Finds the message handler matching the given message type. If no message handler found
     * for the given message type, null is returned.
     *
     * @param systemId The message type to find the message handler for.
     * @return The message handler matching the given message type, or null if no message handler found.
     */
    public SystemHandler findSystem(byte[] systemId, int offset, int length){
        for(int i = 0; i< systemHandlers.length; i++){
            if(systemHandlers[i].systemIdEquals(systemId, offset, length)){
                return systemHandlers[i];
            }
        }
        return null;
    }

    private boolean isSystemIDKey() {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.SYSTEM_ID_KEY_VALUE;
    }
}
