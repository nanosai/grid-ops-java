package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessage;
import com.nanosai.gridops.ion.read.IonReader;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class ProtocolReactorMock extends ProtocolReactor {

    public boolean handleMessageCalled = false;


    public ProtocolReactorMock(byte[] protocolId){
        super(protocolId, new MessageReactor[0]);
    }

    public ProtocolReactorMock(byte[] protocolId, MessageReactor... messageReactors) {
        super(protocolId, messageReactors);
    }


    @Override
    public void react(IonReader reader, IapMessage message) {
        this.handleMessageCalled = true;
    }

}
