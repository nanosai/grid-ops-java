package com.nanosai.gridops.system;

import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class SystemMock extends System {

    public boolean handleMessageCalled = false;

    public SystemMock(byte[] systemId){
        super(systemId, new ProtocolHandler[0]);
    }

    public SystemMock(byte[] systemId, ProtocolHandler... protocolHandlers) {
        super(systemId, protocolHandlers);
    }

    @Override
    public void handleMessage(IonReader reader, MemoryBlock message) {
        this.handleMessageCalled = true;
    }


}
