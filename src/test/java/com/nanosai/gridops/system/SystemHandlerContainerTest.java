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
public class SystemHandlerContainerTest {


    @Test
    public void testFindSystem() {

        SystemHandler systemHandler0 = new SystemHandler(new byte[]{0}) {
            @Override
            public void handleMessage(IonReader reader, MemoryBlock message) {
            }
        };

        SystemHandler systemHandler1 = new SystemHandler(new byte[]{1}) {
            @Override
            public void handleMessage(IonReader reader, MemoryBlock message) {
            }
        };

        SystemContainer systemContainer = new SystemContainer(
                systemHandler0, systemHandler1);

        assertSame(systemHandler0, systemContainer.findSystem(new byte[]{0}, 0, 1));
        assertSame(systemHandler1, systemContainer.findSystem(new byte[]{1}, 0, 1));

        assertNull(systemContainer.findSystem(new byte[]{2}, 0, 1));
    }


    @Test
    public void testHandleMessage() {
        byte systemId = 0;
        SystemHandlerMock system0 = new SystemHandlerMock(new byte[]{systemId});
        assertFalse(system0.handleMessageCalled);

        SystemContainer systemContainer = new SystemContainer(system0);

        MemoryAllocator memoryAllocator = GridOps.memoryAllocator(1024 * 1024, 1024);
        MemoryBlock memoryBlock     = memoryAllocator.getMemoryBlock().allocate(1024);

        writeMessage(systemId, memoryBlock);
        systemContainer.handleMessage(memoryBlock);
        assertTrue(system0.handleMessageCalled);

        system0.handleMessageCalled = false;
        byte unknownSystemId = (byte) 123;
        writeMessage(unknownSystemId, memoryBlock);

        systemContainer.handleMessage(memoryBlock);
        assertFalse(system0.handleMessageCalled);

    }

    private void writeMessage(byte systemId, MemoryBlock memoryBlock) {
        IonWriter writer = new IonWriter();
        writer.setDestination(memoryBlock.memoryAllocator.data, memoryBlock.startIndex);
        writer.setComplexFieldStack(new int[16]);
        writer.writeObjectBeginPush(2);

        writer.writeKeyShort(new byte[]{IapMessageKeys.SYSTEM_ID_KEY_VALUE});
        writer.writeBytes(new byte[]{systemId});

        writer.writeObjectEndPop();

        memoryBlock.writeIndex = writer.destIndex;
    }

}
