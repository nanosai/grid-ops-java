package com.nanosai.gridops;

import com.nanosai.gridops.host.Host;
import com.nanosai.gridops.ion.read.IIonObjectReaderConfigurator;
import com.nanosai.gridops.ion.read.IonObjectReader;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonObjectWriter;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.IMemoryBlockFactory;
import com.nanosai.gridops.mem.MemoryAllocator;
import com.nanosai.gridops.mem.MemoryBlock;
import com.nanosai.gridops.node.MessageReactor;
import com.nanosai.gridops.node.NodeContainer;
import com.nanosai.gridops.node.NodeReactor;
import com.nanosai.gridops.node.ProtocolReactor;
import com.nanosai.gridops.tcp.*;
import com.nanosai.gridops.threadloop.IThreadLoopActor;
import com.nanosai.gridops.threadloop.ThreadLoopBackoff;
import com.nanosai.gridops.threadloop.ThreadLoopDefaultImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jjenkov on 29-08-2016.
 */
public class GridOps {


    public static IonReader ionReader() {
        return new IonReader();
    }

    public static IonReader ionReader(byte[] source, int offset, int length){
        return ionReader().setSource(source, offset, length);
    }

    public static IonReader ionReader(MemoryBlock source){
        return ionReader().setSource(source);
    }

    public static IonWriter ionWriter() {
        return new IonWriter();
    }

    public static IonWriter ionWriter(byte[] destination, int offset) {
        return ionWriter().setDestination(destination, offset);
    }

    public static IonWriter ionWriter(MemoryBlock memoryBlock){
        return ionWriter().setDestination(memoryBlock);
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


    public static TcpMessagePortBuilder tcpMessagePortBuilder() {
        return new TcpMessagePortBuilder();
    }

    public static class TcpMessagePortBuilder {
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

        public TcpMessagePortBuilder incomingMessageBufferSize(int incomingMessageBufferSize) {
            this.incomingMessageBufferSize = incomingMessageBufferSize;
            return this;
        }

        public TcpMessagePortBuilder incomingMessageBufferFreeBlockMaxCount(int incomingMessageBufferFreeBlockMaxCount) {
            this.incomingMessageBufferFreeBlockMaxCount = incomingMessageBufferFreeBlockMaxCount;
            return this;
        }

        public TcpMessagePortBuilder incomingMessageMemoryBlockFactory(IMemoryBlockFactory incomingMessageMemoryBlockFactory) {
            this.incomingMessageMemoryBlockFactory = incomingMessageMemoryBlockFactory;
            return this;
        }

        public TcpMessagePortBuilder incomingMessageMemoryAllocator(MemoryAllocator incomingMessageMemoryAllocator) {
            this.incomingMessageMemoryAllocator = incomingMessageMemoryAllocator;
            return this;
        }

        public TcpMessagePortBuilder setMessageReaderFactory(IMessageReaderFactory messageReaderFactory) {
            this.messageReaderFactory = messageReaderFactory;
            return this;
        }

        public TcpMessagePortBuilder outgoingMessageBufferSize(int outgoingMessageBufferSize) {
            this.outgoingMessageBufferSize = outgoingMessageBufferSize;
            return this;
        }

        public TcpMessagePortBuilder outgoingMessageBufferFreeBlockMaxCount(int outgoingMessageBufferFreeBlockMaxCount) {
            this.outgoingMessageBufferFreeBlockMaxCount = outgoingMessageBufferFreeBlockMaxCount;
            return this;
        }

        public TcpMessagePortBuilder outgoingMessageMemoryBlockFactory(IMemoryBlockFactory outgoingMessageMemoryBlockFactory) {
            this.outgoingMessageMemoryBlockFactory = outgoingMessageMemoryBlockFactory;
            return this;
        }

        public TcpMessagePortBuilder outgoingMessageMemoryAllocator(MemoryAllocator outgoingMessageMemoryAllocator) {
            this.outgoingMessageMemoryAllocator = outgoingMessageMemoryAllocator;
            return this;
        }

        public TcpMessagePortBuilder newSocketsQueue(BlockingQueue blockingQueue) {
            this.newSocketsQueue = blockingQueue;
            return this;
        }

        public TcpMessagePortBuilder tcpServer(TcpServer tcpServer){
            newSocketsQueue(tcpServer.getSocketQueue());
            return this;
        }



        public TcpMessagePort build() throws IOException {
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

            return new TcpMessagePort(
                    this.newSocketsQueue,
                    this.messageReaderFactory,
                    this.incomingMessageMemoryAllocator,
                    this.outgoingMessageMemoryAllocator
                    );
        }
    }


    // NodeContainer
    public static NodeContainer nodeContainer() {
        return new NodeContainer();
    }

    // NodeReactor
    public static NodeReactor nodeReactor(byte[] nodeId) {
        return new NodeReactor(nodeId);
    }

    // ProtocolReactor
    public static ProtocolReactor protocolReactor(byte[] protocolId, byte[] protocolVersion){
        return new ProtocolReactor(protocolId, protocolVersion);
    }


    public static HostBuilder hostBuilder() {
        return new HostBuilder();
    }

    public static class HostBuilder {

        private TcpMessagePort tcpMessagePort;
        private NodeContainer  nodeContainer;

        public HostBuilder tcpSocketsPort(TcpMessagePort port){
            this.tcpMessagePort = port;
            return this;
        }

        public HostBuilder nodeContainer(NodeContainer nodeContainer){
            this.nodeContainer = nodeContainer;
            return this;
        }

        public Host build() {
            return new Host(this.tcpMessagePort, this.nodeContainer);
        }

        public Host buildAndStart() {
            Host host = build();
            new Thread(host).start();
            return host;
        }


    }


    public static ThreadLoopBuilder threadLoopBuilder() {
        return new ThreadLoopBuilder();
    }
    public static class ThreadLoopBuilder  {

        private List<IThreadLoopActor> threadLoopActorList = new ArrayList<>();
        private ThreadLoopBackoff threadLoopBackoff = null;

        public ThreadLoopBuilder addThreadLoopActor(IThreadLoopActor actor){
            this.threadLoopActorList.add(actor);
            return this;
        }

        public ThreadLoopBuilder backoff() {
            this.threadLoopBackoff = new ThreadLoopBackoff(1000, 50000, 10);
            return this;
        }

        public ThreadLoopBuilder backoff(int sleepTimeMin, int sleepTimeMax, int step) {
            this.threadLoopBackoff = new ThreadLoopBackoff(sleepTimeMin, sleepTimeMax, step);
            return this;
        }




        public ThreadLoopDefaultImpl build(){
            IThreadLoopActor[] actors = new IThreadLoopActor[threadLoopActorList.size()];
            for(int i=0; i<threadLoopActorList.size(); i++){
                actors[i] = threadLoopActorList.get(i);
            }

            return new ThreadLoopDefaultImpl(this.threadLoopBackoff, actors);
        }

        public ThreadLoopDefaultImpl buildAndStart() {
            ThreadLoopDefaultImpl threadLoop = build();
            new Thread(threadLoop).start();
            return threadLoop;
        }
    }




}
