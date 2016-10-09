package com.nanosai.gridops.system;

import com.nanosai.gridops.iap.IapMessage;
import com.nanosai.gridops.ion.read.IonReader;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class MessageReactorMock extends MessageReactor {

    boolean handleMessageCalled = false;


    public MessageReactorMock(byte[] messageType) {
        super(messageType);
    }

    @Override
    public void react(IonReader reader, IapMessage message) {
        this.handleMessageCalled = true;
    }

}
