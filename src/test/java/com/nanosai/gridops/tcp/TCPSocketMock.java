package com.nanosai.gridops.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jjenkov on 08-09-2016.
 */
public class TCPSocketMock extends TCPSocket {

    public byte[] bytesToRead = null;
    public int    offset  = 0;
    public int    length  = 0;

    public int    windowSize = 0; // window meaning a block of bytes.
    public int    bytesRead  = 0;

    public int    doSocketReadCallCount = 0;


    public TCPSocketMock(TCPSocketPool tcpSocketPool) {
        super(tcpSocketPool);
    }

    public void reset() {
        this.offset     = 0;
        this.length     = 0;
        this.windowSize = 0;
        this.bytesRead  = 0;
        this.doSocketReadCallCount = 0;
    }

    @Override
    protected int doSocketRead(ByteBuffer destinationBuffer) throws IOException {
        this.doSocketReadCallCount++;

        int bytesToRead = Math.min(this.length - this.bytesRead, this.windowSize);

        destinationBuffer.put(this.bytesToRead, this.offset + this.bytesRead, bytesToRead);
        this.bytesRead += bytesToRead;


        return bytesToRead;
    }
}
