package com.nanosai.gridops.system;

import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class MessageReactorMock extends MessageReactor {

    boolean handleMessageCalled = false;


    public MessageReactorMock(int messageType) {
        super(messageType);
    }

    @Override
    public void react(IonReader reader, MemoryBlock message) {
        this.handleMessageCalled = true;
    }

}
