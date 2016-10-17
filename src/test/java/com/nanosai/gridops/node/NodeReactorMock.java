package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageFields;
import com.nanosai.gridops.ion.read.IonReader;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class NodeReactorMock extends NodeReactor {

    public boolean handleMessageCalled = false;

    public NodeReactorMock(byte[] systemId){
        super(systemId, new ProtocolReactor[0]);
    }

    public NodeReactorMock(byte[] systemId, ProtocolReactor... protocolReactors) {
        super(systemId, protocolReactors);
    }

    @Override
    public void react(IonReader reader, IapMessageFields message) {
        this.handleMessageCalled = true;
    }


}
