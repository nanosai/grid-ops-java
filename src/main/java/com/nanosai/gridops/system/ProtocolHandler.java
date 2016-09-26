package com.nanosai.gridops.system;

import com.nanosai.gridops.iap.IapMessage;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 23-09-2016.
 */
public class ProtocolHandler {

    //consider using a byte[] instad - for more complex protocol names than numbers.
    public int protocolId = 0;

    private MessageHandler[] messageHandlers = null;


    public ProtocolHandler(int protocolId, MessageHandler ... messageHandlers) {
        this.protocolId = protocolId;
        this.messageHandlers = messageHandlers;
    }

    public void handleMessage(IonReader reader, MemoryBlock message){
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isMessageTypeKey(reader)){
                reader.nextParse();
                int messageType = (int) reader.readInt64();

                MessageHandler messageHandlerForMessageType =
                        findMessageHandler(messageType);

                if(messageHandlerForMessageType != null){
                    reader.nextParse();
                    messageHandlerForMessageType.handleMessage(reader, message);
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
    protected MessageHandler findMessageHandler(int messageType){
        for(int i=0; i<messageHandlers.length; i++){
            if(messageType == messageHandlers[i].messageType){
                return messageHandlers[i];
            }
        }
        return null;
    }


    private boolean isMessageTypeKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessage.MESSAGE_TYPE_KEY_VALUE;
    }
}
