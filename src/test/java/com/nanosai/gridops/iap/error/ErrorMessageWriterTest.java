package com.nanosai.gridops.iap.error;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.iap.IapMessageKeys;
import com.nanosai.gridops.iap.IapSemanticProtocolIds;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jjenkov on 01-10-2016.
 */
public class ErrorMessageWriterTest {

    @Test
    public void test() {

        MemoryAllocator memoryAllocator = GridOps.memoryAllocator(1024, 16);
        MemoryBlock memoryBlock = memoryAllocator.getMemoryBlock();

        ErrorMessageWriter writer = new ErrorMessageWriter();

        writer.writeErrorMessage(memoryBlock, 123, "Error");

        byte[] dest = memoryBlock.memoryAllocator.data;
        int index = 0;
        assertEquals( (IonFieldTypes.OBJECT << 4 | 2), 255 & dest[index++] );
        assertEquals( 0, 255 & dest[index++] );
        assertEquals(26, 255 & dest[index++] );

        assertEquals( (IonFieldTypes.KEY_SHORT << 4 | 1), 255 & dest[index++] );
        assertEquals( IapMessageKeys.SEMANTIC_PROTOCOL_ID,   255 & dest[index++] );

        assertEquals( (IonFieldTypes.BYTES << 4 | 1), 255 & dest[index++] );
        assertEquals( 1, 255 & dest[index++] );
        assertEquals( IapSemanticProtocolIds.ERROR_PROTOCOL_ID,   255 & dest[index++] );

        assertEquals( (IonFieldTypes.KEY_SHORT << 4 | 1), 255 & dest[index++] );
        assertEquals( IapMessageKeys.SEMANTIC_PROTOCOL_VERSION,   255 & dest[index++] );

        assertEquals( (IonFieldTypes.BYTES << 4 | 1), 255 & dest[index++] );
        assertEquals( 1, 255 & dest[index++] );
        assertEquals( 0, 255 & dest[index++] );

        assertEquals( (IonFieldTypes.KEY_SHORT << 4 | 1), 255 & dest[index++] );
        assertEquals( IapMessageKeys.MESSAGE_TYPE,   255 & dest[index++] );

        assertEquals( (IonFieldTypes.INT_POS << 4 | 1), 255 & dest[index++] );
        assertEquals( ErrorMessageTypes.ERROR_MESSAGE_TYPE, 255 & dest[index++] );

        assertEquals( (IonFieldTypes.KEY_SHORT << 4 | 1), 255 & dest[index++] );
        assertEquals( 10,   255 & dest[index++] );

        assertEquals( (IonFieldTypes.INT_POS << 4 | 1), 255 & dest[index++] );
        assertEquals( 123,   255 & dest[index++] );

        assertEquals( (IonFieldTypes.KEY_SHORT << 4 | 1), 255 & dest[index++] );
        assertEquals( 11,   255 & dest[index++] );

        assertEquals( (IonFieldTypes.UTF_8_SHORT << 4 | 5), 255 & dest[index++] );
        assertEquals( 'E',   255 & dest[index++] );
        assertEquals( 'r',   255 & dest[index++] );
        assertEquals( 'r',   255 & dest[index++] );
        assertEquals( 'o',   255 & dest[index++] );
        assertEquals( 'r',   255 & dest[index++] );

    }
}
