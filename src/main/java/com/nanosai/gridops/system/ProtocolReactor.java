package com.nanosai.gridops.system;

import com.nanosai.gridops.iap.IapMessageKeys;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 23-09-2016.
 */
public class ProtocolReactor {

    //consider using a byte[] instad - for more complex protocol names than numbers.
    public int protocolId = 0;

    private MessageReactor[] messageReactors = null;


    public ProtocolReactor(int protocolId, MessageReactor... messageReactors) {
        this.protocolId = protocolId;
        this.messageReactors = messageReactors;
    }

    public void react(IonReader reader, MemoryBlock message){
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isMessageTypeKey(reader)){
                reader.nextParse();
                int messageType = (int) reader.readInt64();

                MessageReactor messageHandlerForMessageType =
                        findMessageHandler(messageType);

                if(messageHandlerForMessageType != null){
                    reader.nextParse();
                    messageHandlerForMessageType.react(reader, message);
                }
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
    protected MessageReactor findMessageHandler(int messageType){
        for(int i = 0; i< messageReactors.length; i++){
            if(messageType == messageReactors[i].messageType){
                return messageReactors[i];
            }
        }
        return null;
    }


    private boolean isMessageTypeKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.MESSAGE_TYPE;
    }
}
