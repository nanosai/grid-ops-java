package com.nanosai.gridops.iap;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jjenkov on 22-10-2016.
 */
public class IapMessageFieldsTest {

    @Test
    public void testEqualsMethods(){
        IapMessageFields messageFields = createIapMessageFields();

        int length = writeMessageFields(messageFields);

        readMessageFields(messageFields, length);

        assertTrue (messageFields.equalsReceiverNodeId(new byte[]{99}));
        assertFalse(messageFields.equalsReceiverNodeId(new byte[]{11}));

        assertTrue (messageFields.equalsSemanticProtocolId(new byte[]{88}));
        assertFalse(messageFields.equalsSemanticProtocolId(new byte[]{11}));

        assertTrue (messageFields.equalsSemanticProtocolVersion(new byte[]{77}));
        assertFalse(messageFields.equalsSemanticProtocolVersion(new byte[]{11}));

        assertTrue (messageFields.equalsMessageType(new byte[]{66}));
        assertFalse(messageFields.equalsMessageType(new byte[]{11}));

    }

    private void readMessageFields(IapMessageFields messageFields, int offset) {
        IonReader ionReader = GridOps.ionReader().setSource(messageFields.data, 0, offset);

        ionReader.nextParse();
        ionReader.moveInto();
        ionReader.nextParse();

        IapMessageFieldsReader.read(ionReader, messageFields);
    }

    private int writeMessageFields(IapMessageFields messageFields) {
        IonWriter ionWriter = GridOps.ionWriter().setNestedFieldStack(new int[2]).setDestination(messageFields.data, 0);
        ionWriter.writeObjectBeginPush(1);

        IapMessageFieldsWriter.writeReceiverNodeId         (ionWriter, new byte[]{99});
        IapMessageFieldsWriter.writeSemanticProtocolId     (ionWriter, new byte[]{88});
        IapMessageFieldsWriter.writeSemanticProtocolVersion(ionWriter, new byte[]{77});
        IapMessageFieldsWriter.writeMessageType            (ionWriter, new byte[]{66});

        ionWriter.writeObjectEndPop();
        return ionWriter.destIndex;
    }

    private IapMessageFields createIapMessageFields() {
        IapMessageFields messageFields = new IapMessageFields();
        messageFields.data = new byte[128];
        return messageFields;
    }
}
