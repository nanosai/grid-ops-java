package com.nanosai.gridops.tcp;


import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 26-05-2016.
 */

public class TCPMessage extends MemoryBlock {

    public long socketId = 0;

    public TCPSocket tcpSocket = null;

    public TCPMessage(MemoryAllocator memoryAllocator) {
        super(memoryAllocator);
    }


}
