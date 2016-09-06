package com.nanosai.gridops.mem;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jjenkov on 04-09-2016.
 */
public class MemoryAllocatorTest {


    @Test
    public void testInstantiation_customMemoryBlockFactory() {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024],
                (allocator) -> new MemoryBlock(allocator)
        );

        assertEquals(1024 * 1024, memoryAllocator.capacity());
        assertEquals(1024 * 1024, memoryAllocator.freeCapacity());
        assertEquals(1, memoryAllocator.freeBlockCount());

        MemoryBlock block1 = memoryAllocator.getMemoryBlock();
        MemoryBlock block2 = memoryAllocator.getMemoryBlock();

        assertEquals(1024 * 1024, memoryAllocator.capacity());
        assertEquals(1024 * 1024, memoryAllocator.freeCapacity());
        assertEquals(1, memoryAllocator.freeBlockCount());

        assertNotNull(block1);
        assertNotNull(block2);
    }


    @Test
    public void testInstantiation_defaultMemoryBlockFactory() {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024]);

        MemoryBlock block1 = memoryAllocator.getMemoryBlock();
        MemoryBlock block2 = memoryAllocator.getMemoryBlock();

        assertNotNull(block1);
        assertNotNull(block2);
    }

    @Test
    public void testAllocation() {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024]);

        MemoryBlock block1 = memoryAllocator.getMemoryBlock();
        block1.allocate(128);

        assertEquals(128, block1.lengthAllocated());

        assertEquals(1024 * 1024, memoryAllocator.capacity());
        assertEquals(1024 * 1024 - 128, memoryAllocator.freeCapacity());
        assertEquals(1, memoryAllocator.freeBlockCount());
    }

    @Test
    public void testFree() {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024]);

        MemoryBlock block1 = memoryAllocator.getMemoryBlock();
        block1.allocate(128);

        assertEquals(128, block1.lengthAllocated());

        assertEquals(1024 * 1024, memoryAllocator.capacity());
        assertEquals(1024 * 1024 - 128, memoryAllocator.freeCapacity());
        assertEquals(1, memoryAllocator.freeBlockCount());

        block1.free();

        assertEquals(1024 * 1024, memoryAllocator.capacity());
        assertEquals(1024 * 1024, memoryAllocator.freeCapacity());
        assertEquals(2, memoryAllocator.freeBlockCount());

    }

    @Test
    public void testDefragment_simple() {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024]);

        MemoryBlock block1 = memoryAllocator.getMemoryBlock();
        block1.allocate(128);

        block1.free();

        assertEquals(1024 * 1024, memoryAllocator.capacity());
        assertEquals(1024 * 1024, memoryAllocator.freeCapacity());
        assertEquals(2, memoryAllocator.freeBlockCount());

        memoryAllocator.defragment();

        assertEquals(1024 * 1024, memoryAllocator.capacity());
        assertEquals(1024 * 1024, memoryAllocator.freeCapacity());
        assertEquals(1, memoryAllocator.freeBlockCount());
    }

    @Test
    public void testDefragment_complex() {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024]);

        MemoryBlock block1 = memoryAllocator.getMemoryBlock();
        MemoryBlock block2 = memoryAllocator.getMemoryBlock();
        MemoryBlock block3 = memoryAllocator.getMemoryBlock();
        MemoryBlock block4 = memoryAllocator.getMemoryBlock();
        MemoryBlock block5 = memoryAllocator.getMemoryBlock();

        block1.allocate(128);
        block2.allocate(128);
        block3.allocate(128);
        block4.allocate(128);
        block5.allocate(128);

        assertEquals(1024 * 1024, memoryAllocator.capacity());
        assertEquals(1024 * 1024 - (5 * 128), memoryAllocator.freeCapacity());
        assertEquals(1, memoryAllocator.freeBlockCount());

        block2.free();
        block4.free();

        assertEquals(1024 * 1024, memoryAllocator.capacity());
        assertEquals(1024 * 1024 - (3 * 128), memoryAllocator.freeCapacity());
        assertEquals(3, memoryAllocator.freeBlockCount());

        memoryAllocator.defragment();
        assertEquals(3, memoryAllocator.freeBlockCount()); //defrag not possible with disjunct free blocks.

        block1.free();
        memoryAllocator.defragment();
        assertEquals(3, memoryAllocator.freeBlockCount()); //block 1 and 2 merged into 1 free block
        assertEquals(1024 * 1024 - (2 * 128), memoryAllocator.freeCapacity());

        block3.free();
        memoryAllocator.defragment();
        assertEquals(2, memoryAllocator.freeBlockCount()); //block 1+2, 3 and 4 merged into 1 free block

        block5.free();
        memoryAllocator.defragment();
        assertEquals(1, memoryAllocator.freeBlockCount()); //block 1+2+3+4+5 and rest free space merged into 1 free block

    }

    @Test
    public void testNoFreeBlockFound() {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024], new long[1024]);

        MemoryBlock block1 = memoryAllocator.getMemoryBlock().allocate(1024);

        assertTrue(block1.startIndex != -1); //allocation succeeded

        MemoryBlock block2 = memoryAllocator.getMemoryBlock().allocate(1024);

        assertTrue(block2.startIndex == -1); //allocation failed

    }

    @Test
    public void testFreeBlockDefragLimitReached() {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024]);

        assertEquals(10000, memoryAllocator.freeBlockCountDefragLimit());
        memoryAllocator.freeBlockCountDefragLimit(10);
        assertEquals(10, memoryAllocator.freeBlockCountDefragLimit());



        MemoryBlock[] memoryBlock = new MemoryBlock[1024];

        for(int i=0; i<memoryBlock.length; i++){
            memoryBlock[i] = memoryAllocator.getMemoryBlock().allocate(128);
        }
        for(int i=0; i<memoryBlock.length; i++){
            memoryBlock[i].free();
            assertTrue( 10 > memoryAllocator.freeBlockCount());
        }
    }

    @Test
    public void testMemoryBlockPooling() {
        MemoryAllocator memoryAllocator = new MemoryAllocator(
                new byte[1024 * 1024], new long[1024]);

        MemoryBlock block1 = memoryAllocator.getMemoryBlock();

        block1.free();

        MemoryBlock block2 = memoryAllocator.getMemoryBlock();  //should return pooled instance

        assertSame(block1, block2);

        MemoryBlock block3 = memoryAllocator.getMemoryBlock();  //should NOT return pooled instance

        assertNotSame(block2, block3);
    }

}
