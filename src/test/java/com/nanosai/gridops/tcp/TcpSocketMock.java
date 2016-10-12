package com.nanosai.gridops.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jjenkov on 08-09-2016.
 */
public class TcpSocketMock extends TcpSocket {

    /* Read related variables */
    public byte[] byteSource = null;
    public int    sourceOffset = 0;
    public int    sourceLength = 0;

    public int    readWindowSize = 0; // window meaning a block of bytes.
    public int    bytesRead  = 0;

    public int    doSocketReadCallCount = 0;


    /* Write related variables */
    public byte[] byteDest = null;
    public int    destOffset = 0;
    public int    destLength = 0;

    public int    writeWindowSize = 0; // window meaning a block of bytes.

    public int    doSocketWriteCallCount = 0;

    public int    bytesWritten = 0;
    public int    writeCap = Integer.MAX_VALUE;



    public TcpSocketMock(TcpSocketPool tcpSocketPool) {
        super(tcpSocketPool);
    }

    public void reset() {
        this.sourceOffset = 0;
        this.sourceLength = 0;
        this.readWindowSize = 0;
        this.bytesRead  = 0;
        this.doSocketReadCallCount = 0;
    }

    @Override
    protected int doSocketRead(ByteBuffer destinationBuffer) throws IOException {
        this.doSocketReadCallCount++;

        int bytesToRead = Math.min(this.sourceLength - this.bytesRead, this.readWindowSize);

        destinationBuffer.put(this.byteSource, this.sourceOffset + this.bytesRead, bytesToRead);
        this.bytesRead += bytesToRead;


        return bytesToRead;
    }

    @Override
    public int doSocketWrite(ByteBuffer byteBuffer) throws IOException {
        this.doSocketWriteCallCount++;
        if(this.bytesWritten >= this.writeCap){
            return 0; //imitate a situation where not all of the message could be written to the underlying socket.
        }
        int length = Math.min(byteBuffer.remaining(), this.writeWindowSize);

        byteBuffer.get(this.byteDest, this.destOffset + this.destLength, length);
        this.destLength += length;

        this.bytesWritten += length;

        return length;
    }

}
