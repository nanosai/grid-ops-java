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
public class IapMessageBaseTest {

    @Test
    public void testEqualsMethods(){
        IapMessageBase messageBase = new IapMessageBase();

        messageBase.receiverNodeIdSource = new byte[]{99};
        messageBase.receiverNodeIdOffset = 0;
        messageBase.receiverNodeIdLength = messageBase.receiverNodeIdSource.length;

        messageBase.semanticProtocolIdSource = new byte[]{88};
        messageBase.semanticProtocolIdOffset = 0;
        messageBase.semanticProtocolIdLength = messageBase.semanticProtocolIdSource.length;

        messageBase.semanticProtocolVersionSource = new byte[]{77};
        messageBase.semanticProtocolVersionOffset = 0;
        messageBase.semanticProtocolVersionLength = messageBase.semanticProtocolVersionSource.length;

        messageBase.messageTypeSource = new byte[]{66};
        messageBase.messageTypeOffset = 0;
        messageBase.messageTypeLength = messageBase.messageTypeSource.length;

        IonWriter writer = GridOps.ionWriter().setNestedFieldStack(new int[2]).setDestination(new byte[128], 0);
        writer.writeObjectBeginPush(1);
        messageBase.write(writer);
        writer.writeObjectEndPop();

        IonReader reader = GridOps.ionReader().setSource(writer.dest, 0, writer.index);
        reader.nextParse().moveInto();

        IapMessageBase messageBase2 = new IapMessageBase();
        messageBase2.read(reader);


        assertTrue (messageBase.equalsReceiverNodeId(new byte[]{99}));
        assertFalse(messageBase.equalsReceiverNodeId(new byte[]{11}));

        assertTrue (messageBase.equalsSemanticProtocolId(new byte[]{88}));
        assertFalse(messageBase.equalsSemanticProtocolId(new byte[]{11}));

        assertTrue (messageBase.equalsSemanticProtocolVersion(new byte[]{77}));
        assertFalse(messageBase.equalsSemanticProtocolVersion(new byte[]{11}));

        assertTrue (messageBase.equalsMessageType(new byte[]{66}));
        assertFalse(messageBase.equalsMessageType(new byte[]{11}));

    }

    /*
    private void readMessageFields(IapMessageBase messageBase, int offset) {
        IonReader ionReader = GridOps.ionReader().setSource(messageBase.data, 0, offset);

        ionReader.nextParse();
        ionReader.moveInto();
        ionReader.nextParse();

        messageBase.read(ionReader);
    }
    */

    /*
    private int writeMessageFields(IapMessageBase messageBase) {
        IonWriter ionWriter = GridOps.ionWriter().setNestedFieldStack(new int[2]).setDestination(messageBase.data, 0);
        ionWriter.writeObjectBeginPush(1);

        messageBase

        IapMessageBase.writeAccessToken         (ionWriter, new byte[]{99});
        IapMessageBase.writeServiceId     (ionWriter, new byte[]{88});
        IapMessageBase.writeIpAddress(ionWriter, new byte[]{77});
        IapMessageBase.writeTcpPort            (ionWriter, new byte[]{66});

        ionWriter.writeObjectEndPop();
        return ionWriter.index;
    }
    */

}
