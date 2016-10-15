package com.nanosai.gridops;

import com.nanosai.gridops.ion.read.IIonObjectReaderConfigurator;
import com.nanosai.gridops.ion.read.IonObjectReader;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonObjectWriter;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.IMemoryBlockFactory;
import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.node.MessageReactor;
import com.nanosai.gridops.node.NodeContainer;
import com.nanosai.gridops.node.NodeReactor;
import com.nanosai.gridops.node.ProtocolReactor;
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

    public static TcpServerBuilder tcpServerBuilder() {
        return new TcpServerBuilder();
    }

    public static MemoryAllocator memoryAllocator(int sizeInBytes, int maxFreeBlocks){
        return new MemoryAllocator(new byte[sizeInBytes], new long[maxFreeBlocks]);
    }

    public static MemoryAllocator memoryAllocator(int sizeInBytes, int maxFreeBlocks, IMemoryBlockFactory memoryBlockFactory){
        return new MemoryAllocator(new byte[sizeInBytes], new long[maxFreeBlocks], memoryBlockFactory);
    }

    public static TcpSocketsPortBuilder tcpSocketsPortBuilder() {
        return new TcpSocketsPortBuilder();
    }


    public static class TcpServerBuilder {
        private int tcpPort = 1111;
        private int newSocketQueueCapacity = 1024;
        private BlockingQueue newSocketsQueue = null;

        public TcpServerBuilder tcpPort(int tcpPort){
            this.tcpPort = tcpPort;
            return this;
        }

        public TcpServerBuilder newSocketQueueCapacity(int capacity){
            this.newSocketQueueCapacity = capacity;
            return this;
        }

        public TcpServerBuilder newSocketQueue(BlockingQueue newSocketsQueue){
            this.newSocketsQueue = newSocketsQueue;
            return this;
        }

        public TcpServer build() {
            if(this.newSocketsQueue == null) {
                this.newSocketsQueue = new ArrayBlockingQueue(this.newSocketQueueCapacity);
            }
            return new TcpServer(this.tcpPort, this.newSocketsQueue);
        }

        public TcpServer buildAndStart() {
            TcpServer tcpServer = build();
            new Thread(tcpServer).start();
            return tcpServer;
        }
    }


    public static class TcpSocketsPortBuilder {
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

        public TcpSocketsPortBuilder incomingMessageBufferSize(int incomingMessageBufferSize) {
            this.incomingMessageBufferSize = incomingMessageBufferSize;
            return this;
        }

        public TcpSocketsPortBuilder incomingMessageBufferFreeBlockMaxCount(int incomingMessageBufferFreeBlockMaxCount) {
            this.incomingMessageBufferFreeBlockMaxCount = incomingMessageBufferFreeBlockMaxCount;
            return this;
        }

        public TcpSocketsPortBuilder incomingMessageMemoryBlockFactory(IMemoryBlockFactory incomingMessageMemoryBlockFactory) {
            this.incomingMessageMemoryBlockFactory = incomingMessageMemoryBlockFactory;
            return this;
        }

        public TcpSocketsPortBuilder incomingMessageMemoryAllocator(MemoryAllocator incomingMessageMemoryAllocator) {
            this.incomingMessageMemoryAllocator = incomingMessageMemoryAllocator;
            return this;
        }

        public TcpSocketsPortBuilder setMessageReaderFactory(IMessageReaderFactory messageReaderFactory) {
            this.messageReaderFactory = messageReaderFactory;
            return this;
        }

        public TcpSocketsPortBuilder outgoingMessageBufferSize(int outgoingMessageBufferSize) {
            this.outgoingMessageBufferSize = outgoingMessageBufferSize;
            return this;
        }

        public TcpSocketsPortBuilder outgoingMessageBufferFreeBlockMaxCount(int outgoingMessageBufferFreeBlockMaxCount) {
            this.outgoingMessageBufferFreeBlockMaxCount = outgoingMessageBufferFreeBlockMaxCount;
            return this;
        }

        public TcpSocketsPortBuilder outgoingMessageMemoryBlockFactory(IMemoryBlockFactory outgoingMessageMemoryBlockFactory) {
            this.outgoingMessageMemoryBlockFactory = outgoingMessageMemoryBlockFactory;
            return this;
        }

        public TcpSocketsPortBuilder outgoingMessageMemoryAllocator(MemoryAllocator outgoingMessageMemoryAllocator) {
            this.outgoingMessageMemoryAllocator = outgoingMessageMemoryAllocator;
            return this;
        }

        public TcpSocketsPortBuilder newSocketsQueue(BlockingQueue blockingQueue) {
            this.newSocketsQueue = blockingQueue;
            return this;
        }

        public TcpSocketsPortBuilder tcpServer(TcpServer tcpServer){
            newSocketsQueue(tcpServer.getSocketQueue());
            return this;
        }



        public TcpSocketsPort build() throws IOException {
            /*
            if(this.newSocketsQueue == null){
                throw new RuntimeException("The newSocketsQueue must not be null");
            }
            */

            if(this.incomingMessageMemoryBlockFactory == null){
                this.incomingMessageMemoryBlockFactory = (allocator) -> new TcpMessage(allocator);
            }

            if(this.incomingMessageMemoryAllocator == null){
                byte[] buffer     = new byte[this.incomingMessageBufferSize];
                long[] freeBlocks = new long[this.incomingMessageBufferFreeBlockMaxCount];
                this.incomingMessageMemoryAllocator = new MemoryAllocator(buffer, freeBlocks, this.incomingMessageMemoryBlockFactory);
            }

            if(this.outgoingMessageMemoryBlockFactory == null){
                this.outgoingMessageMemoryBlockFactory = (allocator) -> new TcpMessage(allocator);
            }

            if(this.outgoingMessageMemoryAllocator == null){
                byte[] buffer     = new byte[this.outgoingMessageBufferSize];
                long[] freeBlocks = new long[this.outgoingMessageBufferFreeBlockMaxCount];
                this.outgoingMessageMemoryAllocator = new MemoryAllocator(buffer, freeBlocks, this.outgoingMessageMemoryBlockFactory);
            }

            if(this.messageReaderFactory == null){
                this.messageReaderFactory = new IapMessageReaderFactory();
            }

            return new TcpSocketsPort(
                    this.newSocketsQueue,
                    this.messageReaderFactory,
                    this.incomingMessageMemoryAllocator,
                    this.outgoingMessageMemoryAllocator
                    );
        }
    }


    // NodeContainer
    public static NodeContainer nodeContainer(NodeReactor ... nodeReactors) {
        return new NodeContainer(nodeReactors);
    }

    // NodeReactor
    public static NodeReactor nodeReactor(byte[] nodeId, ProtocolReactor ... protocolReactors) {
        return new NodeReactor(nodeId, protocolReactors);
    }

    // ProtocolReactor
    public ProtocolReactor protocolReactor(byte[] protocolId, MessageReactor... messageReactors){
        return new ProtocolReactor(protocolId, messageReactors);
    }





}
