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
public class ProtocolReactorTest {


    @Test
    public void testFindMessageHandler() {

        MessageReactor messageReactor0 = new MessageReactor(0) {
            @Override
            public void react(IonReader reader, MemoryBlock message) {
            }
        };

        MessageReactor messageReactor1 = new MessageReactor(1) {
            @Override
            public void react(IonReader reader, MemoryBlock message) {
            }
        };

        ProtocolReactor protocolReactor = new ProtocolReactor(0, messageReactor0, messageReactor1);

        assertSame(messageReactor0, protocolReactor.findMessageHandler(0));
        assertSame(messageReactor1, protocolReactor.findMessageHandler(1));

        assertNull(protocolReactor.findMessageHandler(2));
    }

    @Test
    public void testHandleMessage() {
        MessageReactorMock messageHandlerMock = new MessageReactorMock(0);
        assertFalse(messageHandlerMock.handleMessageCalled);

        ProtocolReactor protocolReactor = new ProtocolReactor(0, messageHandlerMock);

        MemoryAllocator memoryAllocator = GridOps.memoryAllocator(1024 * 1024, 1024);
        MemoryBlock memoryBlock     = memoryAllocator.getMemoryBlock().allocate(1024);

        byte messageType = 0;
        writeMessage(messageType, memoryBlock);

        IonReader reader = new IonReader();
        reader.setSource(memoryBlock.memoryAllocator.data, memoryBlock.startIndex, memoryBlock.lengthWritten());
        reader.nextParse();

        protocolReactor.react(reader, memoryBlock);
        assertTrue(messageHandlerMock.handleMessageCalled);

        writeMessage((byte) 123, memoryBlock);
        reader.setSource(memoryBlock.memoryAllocator.data, memoryBlock.startIndex, memoryBlock.lengthWritten());
        reader.nextParse();

        messageHandlerMock.handleMessageCalled = false;
        protocolReactor.react(reader, memoryBlock);

        assertFalse(messageHandlerMock.handleMessageCalled);


    }


    private void writeMessage(byte messageType, MemoryBlock memoryBlock) {
        IonWriter writer = new IonWriter();
        writer.setDestination(memoryBlock.memoryAllocator.data, memoryBlock.startIndex);
        writer.setComplexFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        writer.writeKeyShort(new byte[]{IapMessageKeys.MESSAGE_TYPE});
        writer.writeBytes   (new byte[]{messageType});

        //writer.writeObjectEndPop();

        memoryBlock.writeIndex = writer.destIndex;
    }

}
