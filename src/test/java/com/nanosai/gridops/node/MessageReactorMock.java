package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpMessagePort;

/**
 * Created by jjenkov on 25-09-2016.
 */
public class MessageReactorMock extends MessageReactor {

    boolean handleMessageCalled = false;


    public MessageReactorMock(byte[] messageType) {
        super(messageType);
    }

    @Override
    public void react(MemoryBlock message, IonReader reader, IapMessageBase messageBase, TcpMessagePort tcpMessagePort) {
        this.handleMessageCalled = true;
    }

}
