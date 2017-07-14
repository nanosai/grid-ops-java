package com.nanosai.gridops.ion.codec;

import com.nanosai.gridops.codegen.SemanticProtocolDescriptor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 */
public class SemanticProtocolDescriptorTest {


    @Test
    public void testConstructor() {
        SemanticProtocolDescriptor semanticProtocolDescriptor = new SemanticProtocolDescriptor();
        assertNull(semanticProtocolDescriptor.semanticProtocolName);
        assertNull(semanticProtocolDescriptor.semanticProtocolId);
        assertNull(semanticProtocolDescriptor.semanticProtocolVersion);

        semanticProtocolDescriptor = new SemanticProtocolDescriptor("myProtocol", new byte[]{1,2,3}, new byte[]{9,8});
        assertEquals("myProtocol", semanticProtocolDescriptor.semanticProtocolName);
        assertEquals(1, semanticProtocolDescriptor.semanticProtocolId[0]);
        assertEquals(2, semanticProtocolDescriptor.semanticProtocolId[1]);
        assertEquals(3, semanticProtocolDescriptor.semanticProtocolId[2]);
        assertEquals(9, semanticProtocolDescriptor.semanticProtocolVersion[0]);
        assertEquals(8, semanticProtocolDescriptor.semanticProtocolVersion[1]);
    }
}
