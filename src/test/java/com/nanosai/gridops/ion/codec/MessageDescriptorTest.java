package com.nanosai.gridops.ion.codec;

import com.nanosai.gridops.codegen.MessageDescriptor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by jjenkov on 17/06/2017.
 */
public class MessageDescriptorTest {

    @Test
    public void testConstructors() {
        MessageDescriptor messageDescriptor = new MessageDescriptor();
        assertNull(messageDescriptor.messageName);
        assertNull(messageDescriptor.messageType);

        messageDescriptor = new MessageDescriptor("CreateAccount", new byte[]{-1, -1}, MessageDescriptor.REQUEST_MEP_TYPE);
        assertEquals("CreateAccount", messageDescriptor.messageName);
        assertEquals(-1, messageDescriptor.messageType[0]);
        assertEquals(-1, messageDescriptor.messageType[1]);
    }
}
