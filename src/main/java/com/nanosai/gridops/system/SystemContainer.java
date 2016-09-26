package com.nanosai.gridops.system;

import com.nanosai.gridops.iap.IapMessage;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 22-09-2016.
 */
public class SystemContainer {

    private System[] systems = null;

    private IonReader reader         = new IonReader();

    public SystemContainer(System ... systems) {
        this.systems = systems;
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

            System system = findSystem(reader.source, reader.index, reader.fieldLength);

            if(system != null){
                reader.nextParse();
                system.handleMessage(reader, message);
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
    public System findSystem(byte[] systemId, int offset, int length){
        for(int i=0; i<systems.length; i++){
            if(systems[i].systemIdEquals(systemId, offset, length)){
                return systems[i];
            }
        }
        return null;
    }

    private boolean isSystemIDKey() {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessage.SYSTEM_ID_KEY_VALUE;
    }
}
