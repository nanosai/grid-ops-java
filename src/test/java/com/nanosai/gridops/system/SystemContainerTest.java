package com.nanosai.gridops.system;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.iap.IapMessage;
import com.nanosai.gridops.iap.IapMessageKeys;
import com.nanosai.gridops.iap.IapMessageReader;
import com.nanosai.gridops.iap.IapMessageWriter;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jjenkov on 24-09-2016.
 */
public class SystemContainerTest {


    @Test
    public void testFindSystem() {

        SystemReactor systemHandler0 = new SystemReactor(new byte[]{0}) {
            @Override
            public void react(IonReader reader, IapMessage message) {
            }
        };

        SystemReactor systemHandler1 = new SystemReactor(new byte[]{1}) {
            @Override
            public void react(IonReader reader, IapMessage message) {
            }
        };

        SystemContainer systemContainer = new SystemContainer(
                systemHandler0, systemHandler1);

        assertSame(systemHandler0, systemContainer.findSystemReactor(new byte[]{0}, 0, 1));
        assertSame(systemHandler1, systemContainer.findSystemReactor(new byte[]{1}, 0, 1));

        assertNull(systemContainer.findSystemReactor(new byte[]{2}, 0, 1));
    }


    @Test
    public void testReact() {
        byte[] systemId = new byte[]{0};
        SystemReactorMock system0 = new SystemReactorMock(systemId);
        assertFalse(system0.handleMessageCalled);

        SystemContainer systemContainer = new SystemContainer(system0);

        byte[] dest = new byte[1024];

        int length = writeMessage(systemId, dest);
        IonReader reader = new IonReader();
        reader.setSource(dest, 0, length);
        reader.nextParse();

        IapMessage message = new IapMessage();
        message.data = dest;
        IapMessageReader.read(reader, message);

        systemContainer.react(reader, message);
        assertTrue(system0.handleMessageCalled);

        system0.handleMessageCalled = false;
        byte[] unknownSystemId = new byte[]{123};
        writeMessage(unknownSystemId, dest);

        systemContainer.react(reader, message);
        assertFalse(system0.handleMessageCalled);

    }

    private int writeMessage(byte[] systemId, byte[] dest) {
        IonWriter writer = new IonWriter();
        writer.setDestination(dest, 0);
        writer.setComplexFieldStack(new int[16]);
        //writer.writeObjectBeginPush(2);

        IapMessageWriter.writeReceiverSystemId(writer, systemId);

        //writer.writeObjectEndPop();
        return writer.destIndex;
    }

}
