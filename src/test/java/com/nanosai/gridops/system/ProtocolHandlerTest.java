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
public class ProtocolHandlerTest {


    @Test
    public void testFindMessageHandler() {

        MessageHandler messageHandler0 = new MessageHandler(0) {
            @Override
            public void handleMessage(IonReader reader, MemoryBlock message) {
            }
        };

        MessageHandler messageHandler1 = new MessageHandler(1) {
            @Override
            public void handleMessage(IonReader reader, MemoryBlock message) {
            }
        };

        ProtocolHandler protocolHandler = new ProtocolHandler(0, messageHandler0, messageHandler1);

        assertSame(messageHandler0, protocolHandler.findMessageHandler(0));
        assertSame(messageHandler1, protocolHandler.findMessageHandler(1));

        assertNull(protocolHandler.findMessageHandler(2));
    }

    @Test
    public void testHandleMessage() {
        MessageHandlerMock messageHandlerMock = new MessageHandlerMock(0);
        assertFalse(messageHandlerMock.handleMessageCalled);

        ProtocolHandler protocolHandler = new ProtocolHandler(0, messageHandlerMock);

        MemoryAllocator memoryAllocator = GridOps.memoryAllocator(1024 * 1024, 1024);
        MemoryBlock memoryBlock     = memoryAllocator.getMemoryBlock().allocate(1024);

        byte messageType = 0;
        writeMessage(messageType, memoryBlock);

        IonReader reader = new IonReader();
        reader.setSource(memoryBlock.memoryAllocator.data, memoryBlock.startIndex, memoryBlock.lengthWritten());
        reader.nextParse();

        protocolHandler.handleMessage(reader, memoryBlock);
        assertTrue(messageHandlerMock.handleMessageCalled);

        writeMessage((byte) 123, memoryBlock);
        reader.setSource(memoryBlock.memoryAllocator.data, memoryBlock.startIndex, memoryBlock.lengthWritten());
        reader.nextParse();

        messageHandlerMock.handleMessageCalled = false;
        protocolHandler.handleMessage(reader, memoryBlock);

        assertFalse(messageHandlerMock.handleMessageCalled);


    }


    private void writeMessage(byte messageType, MemoryBlock memoryBlock) {
        IonWriter writer = new IonWriter();
        writer.setDestination(memoryBlock.memoryAllocator.data, memoryBlock.startIndex);
        writer.setComplexFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        writer.writeKeyShort(new byte[]{IapMessageKeys.MESSAGE_TYPE_KEY_VALUE});
        writer.writeBytes   (new byte[]{messageType});

        //writer.writeObjectEndPop();

        memoryBlock.writeIndex = writer.destIndex;
    }

}
