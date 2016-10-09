package com.nanosai.gridops.iap;

import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jjenkov on 09-10-2016.
 */
public class IapMessageReaderTest {

    byte[] receiverSystemId        = new byte[] { 0 };
    byte[] semanticProtocolId      = new byte[] { 1 };
    byte[] semanticProtocolVersion = new byte[] { 2 };
    byte[] messageType             = new byte[] { 3 };

     @Test
    public void testReadFullMessage() {
        byte[] dest = new byte[128];

        IonWriter writer = new IonWriter().setDestination(dest, 0);

        IapMessageWriter.writeMessageFields(writer, receiverSystemId, semanticProtocolId, semanticProtocolVersion, messageType);

        IapMessage message = new IapMessage();
        message.data = dest;

        IonReader reader = new IonReader();
        reader.setSource(dest, 0, writer.destIndex);
        reader.nextParse();

        IapMessageReader.read(reader, message);

        assertEquals(4, message.receiverSystemIdOffset);
        assertEquals(1, message.receiverSystemIdLength);
        assertEquals(0, message.data[message.receiverSystemIdOffset]);

        assertEquals(9, message.semanticProtocolIdOffset);
        assertEquals(1, message.semanticProtocolIdLength);
        assertEquals(1, message.data[message.semanticProtocolIdOffset]);

        assertEquals(14, message.semanticProtocolVersionOffset);
        assertEquals( 1, message.semanticProtocolVersionLength);
        assertEquals( 2, message.data[message.semanticProtocolVersionOffset]);

        assertEquals(19, message.messageTypeOffset);
        assertEquals( 1, message.messageTypeLength);
        assertEquals( 3, message.data[message.messageTypeOffset]);


     }

    @Test
    public void testReadPartialMessage() {
        byte[] dest = new byte[128];

        IonWriter writer = new IonWriter().setDestination(dest, 0);

        IapMessageWriter.writeSemanticProtocolId(writer, semanticProtocolId);
        IapMessageWriter.writeMessageType       (writer, messageType);

        IapMessage message = new IapMessage();
        message.data = dest;

        IonReader reader = new IonReader();
        reader.setSource(dest, 0, writer.destIndex);
        reader.nextParse();

        IapMessageReader.read(reader, message);

        assertEquals(0, message.receiverSystemIdOffset);
        assertEquals(0, message.receiverSystemIdLength);

        assertEquals(4, message.semanticProtocolIdOffset);
        assertEquals(1, message.semanticProtocolIdLength);
        assertEquals(1, message.data[message.semanticProtocolIdOffset]);

        assertEquals(0, message.semanticProtocolVersionOffset);
        assertEquals(0, message.semanticProtocolVersionLength);

        assertEquals(9, message.messageTypeOffset);
        assertEquals(1, message.messageTypeLength);
        assertEquals(3, message.data[message.messageTypeOffset]);
    }

    @Test
    public void testReadEmptyMessage() {
        byte[] dest = new byte[128];

        IonWriter writer = new IonWriter().setDestination(dest, 0);

        //IapMessageWriter.writeSemanticProtocolId(writer, semanticProtocolId);
        //IapMessageWriter.writeMessageType       (writer, messageType);

        IapMessage message = new IapMessage();
        message.data = dest;

        IonReader reader = new IonReader();
        reader.setSource(dest, 0, writer.destIndex);
        reader.nextParse();

        IapMessageReader.read(reader, message);

        assertEquals(0, message.receiverSystemIdOffset);
        assertEquals(0, message.receiverSystemIdLength);

        assertEquals(0, message.semanticProtocolIdOffset);
        assertEquals(0, message.semanticProtocolIdLength);

        assertEquals(0, message.semanticProtocolVersionOffset);
        assertEquals(0, message.semanticProtocolVersionLength);

        assertEquals(0, message.messageTypeOffset);
        assertEquals(0, message.messageTypeLength);



    }
}
