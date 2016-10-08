package com.nanosai.gridops.system;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.iap.IapMessageKeys;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jjenkov on 24-09-2016.
 */
public class SystemReactorTest {


    @Test
    public void testFindProtocolHandler() {
        ProtocolReactor protocolReactor0 = new ProtocolReactor(0) {
            @Override
            public void handleMessage(IonReader reader, MemoryBlock message) {
            }
        };

        ProtocolReactor protocolReactor1 = new ProtocolReactor(1) {
            @Override
            public void handleMessage(IonReader reader, MemoryBlock message) {
            }
        };

        SystemReactor systemHandler = new SystemReactor(new byte[]{0}, protocolReactor0, protocolReactor1);

        assertSame(protocolReactor0, systemHandler.findProtocolHandler(0));
        assertSame(protocolReactor1, systemHandler.findProtocolHandler(1));

        assertNull(systemHandler.findProtocolHandler(2));
    }

    @Test
    public void testHandleMessage(){
        ProtocolReactorMock protocolHandlerMock = new ProtocolReactorMock(0);
        assertFalse(protocolHandlerMock.handleMessageCalled);

        SystemReactor systemHandler = new SystemReactor(new byte[]{0}, protocolHandlerMock);

        MemoryAllocator memoryAllocator = GridOps.memoryAllocator(1024 * 1024, 1024);
        MemoryBlock memoryBlock     = memoryAllocator.getMemoryBlock().allocate(1024);

        byte semanticProtocolId = 0;
        writeMessage(semanticProtocolId, memoryBlock);

        IonReader reader = new IonReader();
        reader.setSource(memoryBlock.memoryAllocator.data, memoryBlock.startIndex, memoryBlock.lengthWritten());
        reader.nextParse();

        systemHandler.handleMessage(reader, memoryBlock);
        assertTrue(protocolHandlerMock.handleMessageCalled);

        writeMessage((byte) 123, memoryBlock);
        reader.setSource(memoryBlock.memoryAllocator.data, memoryBlock.startIndex, memoryBlock.lengthWritten());
        reader.nextParse();
        protocolHandlerMock.handleMessageCalled = false;

        systemHandler.handleMessage(reader, memoryBlock);
        assertFalse(protocolHandlerMock.handleMessageCalled);
    }


    private void writeMessage(byte semanticProtocolId, MemoryBlock memoryBlock) {
        IonWriter writer = new IonWriter();
        writer.setDestination(memoryBlock.memoryAllocator.data, memoryBlock.startIndex);
        writer.setComplexFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        writer.writeKeyShort(new byte[]{IapMessageKeys.SEMANTIC_PROTOCOL_ID});
        writer.writeBytes(new byte[]{semanticProtocolId});

        //writer.writeObjectEndPop();

        memoryBlock.writeIndex = writer.destIndex;
    }

}
