package com.nanosai.gridops.tcp;


import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 26-05-2016.
 */

public class TcpMessage extends MemoryBlock {

    public long socketId = 0;

    public TcpSocket tcpSocket = null;

    public TcpMessage(MemoryAllocator memoryAllocator) {
        super(memoryAllocator);
    }


}
