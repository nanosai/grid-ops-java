package com.nanosai.gridops.mem;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jjenkov on 04-09-2016.
 */
public class MemoryBlockTest {



    @Test
    public void test() {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1 * 1024 * 1024], new long[1024]);


        MemoryBlock memoryBlock = memoryAllocator.getMemoryBlock();
        memoryBlock.allocate(1024);

        assertFalse(memoryBlock.isComplete());
        memoryBlock.setComplete(true);
        assertTrue(memoryBlock.isComplete());

        memoryBlock.writeIndex = memoryBlock.startIndex + 10;
        assertEquals(10, memoryBlock.lengthWritten());



    }


    public void testGetMemoryBlock() {

    }
}
