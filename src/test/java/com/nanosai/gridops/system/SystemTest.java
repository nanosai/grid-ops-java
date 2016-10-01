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
public class SystemTest {


    @Test
    public void testFindProtocolHandler() {
        ProtocolHandler protocolHandler0 = new ProtocolHandler(0) {
            @Override
            public void handleMessage(IonReader reader, MemoryBlock message) {
            }
        };

        ProtocolHandler protocolHandler1 = new ProtocolHandler(1) {
            @Override
            public void handleMessage(IonReader reader, MemoryBlock message) {
            }
        };

        System system = new System(new byte[]{0}, protocolHandler0, protocolHandler1);

        assertSame(protocolHandler0, system.findProtocolHandler(0));
        assertSame(protocolHandler1, system.findProtocolHandler(1));

        assertNull(system.findProtocolHandler(2));
    }

    @Test
    public void testHandleMessage(){
        ProtocolHandlerMock protocolHandlerMock = new ProtocolHandlerMock(0);
        assertFalse(protocolHandlerMock.handleMessageCalled);

        System system = new System(new byte[]{0}, protocolHandlerMock);

        MemoryAllocator memoryAllocator = GridOps.memoryAllocator(1024 * 1024, 1024);
        MemoryBlock memoryBlock     = memoryAllocator.getMemoryBlock().allocate(1024);

        byte semanticProtocolId = 0;
        writeMessage(semanticProtocolId, memoryBlock);

        IonReader reader = new IonReader();
        reader.setSource(memoryBlock.memoryAllocator.data, memoryBlock.startIndex, memoryBlock.lengthWritten());
        reader.nextParse();

        system.handleMessage(reader, memoryBlock);
        assertTrue(protocolHandlerMock.handleMessageCalled);

        writeMessage((byte) 123, memoryBlock);
        reader.setSource(memoryBlock.memoryAllocator.data, memoryBlock.startIndex, memoryBlock.lengthWritten());
        reader.nextParse();
        protocolHandlerMock.handleMessageCalled = false;

        system.handleMessage(reader, memoryBlock);
        assertFalse(protocolHandlerMock.handleMessageCalled);
    }


    private void writeMessage(byte semanticProtocolId, MemoryBlock memoryBlock) {
        IonWriter writer = new IonWriter();
        writer.setDestination(memoryBlock.memoryAllocator.data, memoryBlock.startIndex);
        writer.setComplexFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        writer.writeKeyShort(new byte[]{IapMessageKeys.SEMANTIC_PROTOCOL_ID_KEY_VALUE});
        writer.writeBytes(new byte[]{semanticProtocolId});

        //writer.writeObjectEndPop();

        memoryBlock.writeIndex = writer.destIndex;
    }

}
