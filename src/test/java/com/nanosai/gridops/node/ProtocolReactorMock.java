package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageFields;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.tcp.TcpSocketsPort;

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
    public void react(IonReader reader, IapMessageFields message, TcpSocketsPort tcpSocketsPort) {
        this.handleMessageCalled = true;
    }

}
