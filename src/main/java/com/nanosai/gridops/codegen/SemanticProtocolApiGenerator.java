package com.nanosai.gridops.codegen;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.tcp.TcpMessage;

/**
 * Generates an API facade class for an IAP semantic protocol.
 */
public class SemanticProtocolApiGenerator {

    public static void main(String[] args){
        SemanticProtocolDescriptor protocolDescriptor = new SemanticProtocolDescriptor(
                "Account", new byte[]{-1, -1}, new byte[]{0}
        );

        MessageDescriptor createAccountRequest = protocolDescriptor.addMessageDescriptor("CreateAccountRequest", new byte[]{0}, MessageDescriptor.REQUEST_MEP_TYPE);
        createAccountRequest.addFieldDescriptor("email"   , IonFieldTypes.UTF_8, new byte[]{16});
        createAccountRequest.addFieldDescriptor("password", IonFieldTypes.UTF_8, new byte[]{17});

        MessageDescriptor createAccountResponse = protocolDescriptor.addMessageDescriptor("CreateAccountResponse", new byte[]{1}, MessageDescriptor.RESPONSE_MEP_TYPE);
        createAccountResponse.addFieldDescriptor("status", IonFieldTypes.INT_POS, new byte[]{18});

        //MessageDescriptor createApiKey  = protocolDescriptor.addMessageDescriptor("CreateApiKeyRequest" , new byte[]{1}, MessageDescriptor.REQUEST_MEP_TYPE);
        //MessageDescriptor createNodeDef = protocolDescriptor.addMessageDescriptor("CreateNodeDefRequest", new byte[]{2}, MessageDescriptor.REQUEST_MEP_TYPE);

        StringBuilder builder = new StringBuilder();
        generate(builder, protocolDescriptor);
        System.out.println(builder);
    }

    public static void generate(StringBuilder target, SemanticProtocolDescriptor protocolDescriptor){
        generateImports(target);

        target.append("public class ");
        target.append(protocolDescriptor.semanticProtocolName);
        target.append("Client {").append("\n");

        generateConstants(target, protocolDescriptor);
        generateFields(target, protocolDescriptor);
        generateConstructor(target, protocolDescriptor);

        generateMethods(target, protocolDescriptor);

        target.append("\n} ");
    }

    private static void generateImports(StringBuilder target) {
        target.append("import com.nanosai.gridops.iap.IapMessageBase;\n");
        target.append("import com.nanosai.gridops.ion.read.IonReader;\n");
        target.append("import com.nanosai.gridops.ion.write.IonWriter;\n");
        target.append("import com.nanosai.gridops.mem.MemoryBlock;\n");
        target.append("import com.nanosai.gridops.mem.MemoryBlockBatch;\n");
        target.append("import com.nanosai.gridops.tcp.TcpMessage;\n");
        target.append("import com.nanosai.gridops.tcp.TcpMessagePort;\n");
        target.append("import com.nanosai.gridops.tcp.TcpSocket;\n");
        target.append("import java.net.InetSocketAddress;\n");
        target.append("\n");
    }

    private static void generateConstants(StringBuilder target, SemanticProtocolDescriptor protocolDescriptor) {
        target.append("\n    protected static final byte[] semanticProtocolId = new byte[]{");
        for(int i=0; i<protocolDescriptor.semanticProtocolId.length; i++){
            if(i > 0){
                target.append(", ");
            }
            target.append(String.valueOf(protocolDescriptor.semanticProtocolId[i]));
        }
        target.append("};");


        target.append("\n    protected static final byte[] semanticProtocolVersion = new byte[]{");
        for(int i=0; i<protocolDescriptor.semanticProtocolVersion.length; i++){
            if(i > 0){
                target.append(", ");
            }
            target.append(String.valueOf(protocolDescriptor.semanticProtocolVersion[i]));
        }
        target.append("};").append("\n");
    }

    private static void generateFields(StringBuilder target, SemanticProtocolDescriptor protocolDescriptor) {
        target.append("\n    protected InetSocketAddress serverAddress = null;");
        target.append("\n    protected TcpMessagePort tcpMessagePort = null;");
        target.append("\n    protected TcpSocket serverTcpSocket = null;");
        target.append("\n    protected IonWriter ionWriter = new IonWriter();");
        target.append("\n    protected IonReader ionReader = new IonReader();");
        target.append("\n    protected MemoryBlockBatch messageBatch = new MemoryBlockBatch(new MemoryBlock[8]);");
        target.append("\n    protected IapMessageBase iapRequestBase  = new IapMessageBase();");
        target.append("\n    protected IapMessageBase iapResponseBase = new IapMessageBase();");
        target.append("\n");
    }

    private static void generateConstructor(StringBuilder target, SemanticProtocolDescriptor protocolDescriptor) {
        target.append("\n    public ");
        target.append(protocolDescriptor.semanticProtocolName);
        target.append("Client(TcpMessagePort tcpMessagePort, InetSocketAddress serverAddress) throws Exception{");
        target.append("\n        this.serverAddress = serverAddress;");
        target.append("\n        this.tcpMessagePort = tcpMessagePort;");
        target.append("\n        this.serverTcpSocket = this.tcpMessagePort.addSocket(serverAddress);");
        target.append("\n        this.iapRequestBase.setSemanticProtocolId(semanticProtocolId);");
        target.append("\n        this.iapRequestBase.setSemanticProtocolVersion(semanticProtocolVersion);");
        target.append("\n        this.ionWriter.setNestedFieldStack(new int[16]);");
        target.append("\n    }");
        target.append("\n");
    }


    private static void generateMethods(StringBuilder target, SemanticProtocolDescriptor protocolDescriptor) {
        for(MessageDescriptor messageDescriptor : protocolDescriptor.messageDescriptors){
            if(messageDescriptor.isRequest() || messageDescriptor.isNotificationOut()){
                generateSendMethod(target, protocolDescriptor, messageDescriptor);
            } else {
                generateReceiveMethod(target, protocolDescriptor, messageDescriptor);
            }
        }
    }

    private static void generateSendMethod(StringBuilder target, SemanticProtocolDescriptor protocolDescriptor, MessageDescriptor messageDescriptor){
        target.append("\n\n    public void ");
        target.append("send");

        target.append(messageDescriptor.getMessageNameFirstCharUppercase());
        target.append("(");

        target.append(messageDescriptor.getMessageNameFirstCharUppercase());
        target.append(" ");
        target.append(messageDescriptor.getMessageNameFirstCharLowercase());

        target.append(") throws Exception {");

        target.append("\n        this.iapRequestBase.setReceiverNodeId(new byte[]{0});");
        target.append("\n        this.iapRequestBase.setMessageType(new byte[]{0});");

        //todo Replace hardcoded memory block size of 1024 - get from somewhere (message descriptor?)
        target.append("\n        TcpMessage outgoingMessage = this.tcpMessagePort.allocateWriteMemoryBlock(1024);");
        target.append("\n");
        target.append("\n        this.ionWriter.setDestination(outgoingMessage);");

        //todo Replace hardcoded lengthLength of 2 with a real lengthLength from somewhere (message descriptor?)
        target.append("\n        this.ionWriter.writeIapMessage(2, iapRequestBase, createAccountRequest, outgoingMessage);");
        target.append("\n        this.tcpMessagePort.writeNowOrEnqueue(this.serverTcpSocket, outgoingMessage);");
        target.append("\n        this.tcpMessagePort.writeBlock();");
        target.append("\n        outgoingMessage.free();");

        target.append("\n    }");

    }

    private static void generateReceiveMethod(StringBuilder target, SemanticProtocolDescriptor protocolDescriptor, MessageDescriptor messageDescriptor){
        target.append("\n\n    public void ");
        target.append("receive");

        target.append(messageDescriptor.getMessageNameFirstCharUppercase());
        target.append("(");
        target.append(messageDescriptor.getMessageNameFirstCharUppercase());
        target.append(" ");
        target.append(messageDescriptor.getMessageNameFirstCharLowercase());

        target.append(") throws Exception {");

        //target.append("\n");
        target.append("\n        this.messageBatch.clear();");
        target.append("\n        this.tcpMessagePort.readBlock(this.messageBatch);");
        target.append("\n        this.ionReader.setSource(this.messageBatch.blocks[0]);");
        target.append("\n        ");
        target.append("\n        this.ionReader.nextParse().moveInto().nextParse();");
        target.append("\n        this.iapResponseBase.read(this.ionReader);");
        target.append("\n");
        target.append("\n        createAccountResponse.read(this.ionReader);");
        target.append("\n");
        target.append("\n        this.messageBatch.blocks[0].free();");

        target.append("\n    }");

    }

    private static void appendFieldAsParameter(StringBuilder target, FieldDescriptor fieldDescriptor) {
        target.append(fieldDescriptor.getFieldType());
        target.append(" ");
        target.append(fieldDescriptor.fieldName);
    }
}
