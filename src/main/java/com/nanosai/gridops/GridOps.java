package com.nanosai.gridops;

import com.nanosai.gridops.ion.read.IIonObjectReaderConfigurator;
import com.nanosai.gridops.ion.read.IonObjectReader;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IIonObjectWriterConfigurator;
import com.nanosai.gridops.ion.write.IonObjectWriter;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.IMemoryBlockFactory;
import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.tcp.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jjenkov on 29-08-2016.
 */
public class GridOps {


    public static IonReader ionReader() {
        return new IonReader();
    }

    public static IonWriter ionWriter() {
        return new IonWriter();
    }

    public static IonObjectReader ionObjectReader(Class targetClass){
        return new IonObjectReader(targetClass);
    }

    public static IonObjectReader ionObjectReader(Class targetClass, IIonObjectReaderConfigurator configurator) {
        return new IonObjectReader(targetClass, configurator);
    }

    public static IonObjectWriter ionObjectWriter(Class targetClass){
        return new IonObjectWriter(targetClass);
    }

    public static TCPServerBuilder tcpServerBuilder() {
        return new TCPServerBuilder();
    }

    public static MemoryAllocator memoryAllocator(int sizeInBytes, int maxFreeBlocks){
        return new MemoryAllocator(new byte[sizeInBytes], new long[maxFreeBlocks]);
    }

    public static MemoryAllocator memoryAllocator(int sizeInBytes, int maxFreeBlocks, IMemoryBlockFactory memoryBlockFactory){
        return new MemoryAllocator(new byte[sizeInBytes], new long[maxFreeBlocks], memoryBlockFactory);
    }

    public static TCPSocketsProxyBuilder tcpSocketsProxyBuilder() {
        return new TCPSocketsProxyBuilder();
    }


    public static class TCPServerBuilder {
        private int tcpPort = 1111;
        private int newSocketQueueCapacity = 1024;
        private BlockingQueue newSocketsQueue = null;

        public TCPServerBuilder tcpPort(int tcpPort){
            this.tcpPort = tcpPort;
            return this;
        }

        public TCPServerBuilder newSocketQueueCapacity(int capacity){
            this.newSocketQueueCapacity = capacity;
            return this;
        }

        public TCPServerBuilder newSocketQueue(BlockingQueue newSocketsQueue){
            this.newSocketsQueue = newSocketsQueue;
            return this;
        }

        public TCPServer build() {
            if(this.newSocketsQueue == null) {
                this.newSocketsQueue = new ArrayBlockingQueue(this.newSocketQueueCapacity);
            }
            return new TCPServer(this.tcpPort, this.newSocketsQueue);
        }

        public TCPServer buildAndStart() {
            TCPServer tcpServer = build();
            new Thread(tcpServer).start();
            return tcpServer;
        }
    }


    public static class TCPSocketsProxyBuilder {
        private int incomingMessageBufferSize = 16 * 1024 * 1024;
        private int incomingMessageBufferFreeBlockMaxCount = 128 * 1024;
        private IMemoryBlockFactory incomingMessageMemoryBlockFactory = null;
        private MemoryAllocator incomingMessageMemoryAllocator = null;

        private IMessageReaderFactory messageReaderFactory = null;

        private int outgoingMessageBufferSize = 16 * 1024 * 1024;
        private int outgoingMessageBufferFreeBlockMaxCount = 128 * 1024;
        private IMemoryBlockFactory outgoingMessageMemoryBlockFactory = null;
        private MemoryAllocator outgoingMessageMemoryAllocator = null;

        private BlockingQueue newSocketsQueue = null;

        public TCPSocketsProxyBuilder incomingMessageBufferSize(int incomingMessageBufferSize) {
            this.incomingMessageBufferSize = incomingMessageBufferSize;
            return this;
        }

        public TCPSocketsProxyBuilder incomingMessageBufferFreeBlockMaxCount(int incomingMessageBufferFreeBlockMaxCount) {
            this.incomingMessageBufferFreeBlockMaxCount = incomingMessageBufferFreeBlockMaxCount;
            return this;
        }

        public TCPSocketsProxyBuilder incomingMessageMemoryBlockFactory(IMemoryBlockFactory incomingMessageMemoryBlockFactory) {
            this.incomingMessageMemoryBlockFactory = incomingMessageMemoryBlockFactory;
            return this;
        }

        public TCPSocketsProxyBuilder incomingMessageMemoryAllocator(MemoryAllocator incomingMessageMemoryAllocator) {
            this.incomingMessageMemoryAllocator = incomingMessageMemoryAllocator;
            return this;
        }

        public TCPSocketsProxyBuilder setMessageReaderFactory(IMessageReaderFactory messageReaderFactory) {
            this.messageReaderFactory = messageReaderFactory;
            return this;
        }

        public TCPSocketsProxyBuilder outgoingMessageBufferSize(int outgoingMessageBufferSize) {
            this.outgoingMessageBufferSize = outgoingMessageBufferSize;
            return this;
        }

        public TCPSocketsProxyBuilder outgoingMessageBufferFreeBlockMaxCount(int outgoingMessageBufferFreeBlockMaxCount) {
            this.outgoingMessageBufferFreeBlockMaxCount = outgoingMessageBufferFreeBlockMaxCount;
            return this;
        }

        public TCPSocketsProxyBuilder outgoingMessageMemoryBlockFactory(IMemoryBlockFactory outgoingMessageMemoryBlockFactory) {
            this.outgoingMessageMemoryBlockFactory = outgoingMessageMemoryBlockFactory;
            return this;
        }

        public TCPSocketsProxyBuilder outgoingMessageMemoryAllocator(MemoryAllocator outgoingMessageMemoryAllocator) {
            this.outgoingMessageMemoryAllocator = outgoingMessageMemoryAllocator;
            return this;
        }

        public TCPSocketsProxyBuilder newSocketsQueue(BlockingQueue blockingQueue) {
            this.newSocketsQueue = blockingQueue;
            return this;
        }

        public TCPSocketsProxyBuilder tcpServer(TCPServer tcpServer){
            newSocketsQueue(tcpServer.getSocketQueue());
            return this;
        }



        public TCPSocketsProxy build() throws IOException {
            /*
            if(this.newSocketsQueue == null){
                throw new RuntimeException("The newSocketsQueue must not be null");
            }
            */

            if(this.incomingMessageMemoryBlockFactory == null){
                this.incomingMessageMemoryBlockFactory = (allocator) -> new TCPMessage(allocator);
            }

            if(this.incomingMessageMemoryAllocator == null){
                byte[] buffer     = new byte[this.incomingMessageBufferSize];
                long[] freeBlocks = new long[this.incomingMessageBufferFreeBlockMaxCount];
                this.incomingMessageMemoryAllocator = new MemoryAllocator(buffer, freeBlocks, this.incomingMessageMemoryBlockFactory);
            }

            if(this.outgoingMessageMemoryBlockFactory == null){
                this.outgoingMessageMemoryBlockFactory = (allocator) -> new TCPMessage(allocator);
            }

            if(this.outgoingMessageMemoryAllocator == null){
                byte[] buffer     = new byte[this.outgoingMessageBufferSize];
                long[] freeBlocks = new long[this.outgoingMessageBufferFreeBlockMaxCount];
                this.outgoingMessageMemoryAllocator = new MemoryAllocator(buffer, freeBlocks, this.outgoingMessageMemoryBlockFactory);
            }

            if(this.messageReaderFactory == null){
                this.messageReaderFactory = new IAPMessageReaderFactory();
            }

            return new TCPSocketsProxy(
                    this.newSocketsQueue,
                    this.messageReaderFactory,
                    this.incomingMessageMemoryAllocator,
                    this.outgoingMessageMemoryAllocator
                    );
        }
    }




}
