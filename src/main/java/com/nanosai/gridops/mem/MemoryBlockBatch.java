package com.nanosai.gridops.mem;

/**
 * Created by jjenkov on 17-10-2016.
 */
public class MemoryBlockBatch {


    public MemoryBlock[] blocks = null;
    public int count = 0;

    public int limit = 64;

    public MemoryBlockBatch(MemoryBlock[] blocks) {
        this.blocks = blocks;
        this.limit  = blocks.length;  // use initial length as limit
    }

    public MemoryBlockBatch(MemoryBlock[] blocks, int limit) {
        this.blocks = blocks;
        this.limit  = limit;
    }

    public MemoryBlockBatch(int initialCapacity){
        this(new MemoryBlock[initialCapacity] );
    }

    public MemoryBlockBatch(int initialCapacity, int limit){
        this(new MemoryBlock[initialCapacity], limit);
    }


    public void add(MemoryBlock memoryBlock){
        if(this.count == this.blocks.length){
            MemoryBlock[] newBlocks = new MemoryBlock[this.blocks.length + 16];
            System.arraycopy(this.blocks, 0, newBlocks, 0, this.blocks.length);

            this.blocks = newBlocks;
        }

        this.blocks[this.count++] = memoryBlock;
    }

    public void clear() {
        this.count = 0;
    }


}
