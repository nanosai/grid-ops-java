package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageFields;
import com.nanosai.gridops.ion.read.IonReader;

/**
 * Created by jjenkov on 23-09-2016.
 */
public abstract class MessageReactor {

    public byte[] messageType = null;

    public MessageReactor(byte[] messageType) {
        this.messageType = messageType;
    }

    public abstract void react(IonReader reader, IapMessageFields message);

}
