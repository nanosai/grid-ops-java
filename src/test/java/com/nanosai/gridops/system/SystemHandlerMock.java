package com.nanosai.gridops.system;

import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class SystemHandlerMock extends SystemHandler {

    public boolean handleMessageCalled = false;

    public SystemHandlerMock(byte[] systemId){
        super(systemId, new ProtocolHandler[0]);
    }

    public SystemHandlerMock(byte[] systemId, ProtocolHandler... protocolHandlers) {
        super(systemId, protocolHandlers);
    }

    @Override
    public void handleMessage(IonReader reader, MemoryBlock message) {
        this.handleMessageCalled = true;
    }


}
