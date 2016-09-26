package com.nanosai.gridops.system;

import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class MessageHandlerMock extends MessageHandler {

    boolean handleMessageCalled = false;


    public MessageHandlerMock(int messageType) {
        super(messageType);
    }

    @Override
    public void handleMessage(IonReader reader, MemoryBlock message) {
        this.handleMessageCalled = true;
    }

}
