package com.nanosai.gridops.system;

import com.nanosai.gridops.iap.IapMessage;
import com.nanosai.gridops.ion.read.IonReader;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class SystemReactorMock extends SystemReactor {

    public boolean handleMessageCalled = false;

    public SystemReactorMock(byte[] systemId){
        super(systemId, new ProtocolReactor[0]);
    }

    public SystemReactorMock(byte[] systemId, ProtocolReactor... protocolReactors) {
        super(systemId, protocolReactors);
    }

    @Override
    public void react(IonReader reader, IapMessage message) {
        this.handleMessageCalled = true;
    }


}
