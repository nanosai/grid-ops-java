package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.tcp.TcpMessagePort;

/**
 * Created by jjenkov on 23-09-2016.
 */
public abstract class MessageReactor {

    public byte[] messageType = null;

    public MessageReactor(byte[] messageType) {
        this.messageType = messageType;
    }

    public abstract void react(MemoryBlock message, IonReader reader, IapMessageBase messageBase, TcpMessagePort tcpMessagePort) throws Exception;

}
