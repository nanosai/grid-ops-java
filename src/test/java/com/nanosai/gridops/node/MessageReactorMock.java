package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageFields;
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
    public void react(IonReader reader, IapMessageFields message) {
        this.handleMessageCalled = true;
    }

}
