package com.nanosai.gridops.system;

import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class ProtocolReactorMock extends ProtocolReactor {

    public boolean handleMessageCalled = false;


    public ProtocolReactorMock(int protocolId){
        super(protocolId, new MessageReactor[0]);
    }

    public ProtocolReactorMock(int protocolId, MessageReactor... messageReactors) {
        super(protocolId, messageReactors);
    }


    @Override
    public void handleMessage(IonReader reader, MemoryBlock message) {
        this.handleMessageCalled = true;
    }
}
