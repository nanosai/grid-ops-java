package com.nanosai.gridops.system;

import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class ProtocolHandlerMock extends ProtocolHandler {

    public boolean handleMessageCalled = false;


    public ProtocolHandlerMock(int protocolId){
        super(protocolId, new MessageHandler[0]);
    }

    public ProtocolHandlerMock(int protocolId, MessageHandler ... messageHandlers) {
        super(protocolId, messageHandlers);
    }


    @Override
    public void handleMessage(IonReader reader, MemoryBlock message) {
        this.handleMessageCalled = true;
    }
}
