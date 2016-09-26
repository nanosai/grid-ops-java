package com.nanosai.gridops.system;

import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 23-09-2016.
 */
public abstract class MessageHandler {

    public int messageType = 0;

    public MessageHandler(int messageType) {
        this.messageType = messageType;
    }

    public abstract void handleMessage(IonReader reader, MemoryBlock message);

}
