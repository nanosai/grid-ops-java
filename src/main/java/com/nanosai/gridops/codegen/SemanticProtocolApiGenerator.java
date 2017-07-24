package com.nanosai.gridops.codegen;

import com.nanosai.gridops.iap.IapMessageBase;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.tcp.TcpMessage;

import java.io.IOException;

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
        target.append("import com.nanosai.gridops.ion.codec.IonCodec;\n");
        target.append("import com.nanosai.gridops.ion.read.IonReader;\n");
        target.append("import com.nanosai.gridops.ion.write.IonWriter;\n");
        target.append("import com.nanosai.gridops.mem.MemoryBlock;\n");
        target.append("import com.nanosai.gridops.mem.MemoryBlockBatch;\n");
        target.append("import com.nanosai.gridops.tcp.TcpMessage;\n");
        target.append("import com.nanosai.gridops.tcp.TcpMessagePort;\n");
        target.append("import com.nanosai.gridops.tcp.TcpSocket;\n");
        target.append("import java.net.InetSocketAddress;\n");
        target.append("import java.io.IOException;\n");
        target.append("\n");
    }

    private static void generateConstants(StringBuilder target, SemanticProtocolDescriptor protocolDescriptor) {
        target.append("\n    protected static final byte[] semanticProtocolId = ");
        appendStringRepOfBytes(target, protocolDescriptor.semanticProtocolId);
        target.append(";");

        target.append("\n    protected static final byte[] semanticProtocolVersion = ");
        appendStringRepOfBytes(target, protocolDescriptor.semanticProtocolVersion);
        target.append(";");

        target.append("\n");
    }

    private static void appendStringRepOfBytes(StringBuilder target, byte[] byteRep) {
        target.append("new byte[]{");
        for(int i=0; i<byteRep.length; i++){
            if(i > 0){
                target.append(", ");
            }
            target.append(String.valueOf(byteRep[i]));
        }
        target.append("}");
    }

    private static void generateKeyValueConstants(StringBuilder target, MessageDescriptor messageDescriptor) {
        for (FieldDescriptor fieldDescriptor : messageDescriptor.fieldDescriptors) {
            target.append("\n    public static final byte[] ");
            appendFieldKeyConstantName(target, fieldDescriptor.fieldName);
            target.append(" = ");
            appendStringRepOfBytes(target, fieldDescriptor.fieldKeyValue);
        }
    }

    private static void appendFieldKeyConstantName(StringBuilder target, String fieldName) {
        target.append(fieldName).append("Key");
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
        generateGeneralSendMethod(target);
        generateGeneralReceiveMethod(target);
        for(MessageDescriptor messageDescriptor : protocolDescriptor.messageDescriptors){
            if(messageDescriptor.isRequest() || messageDescriptor.isNotificationOut()){
                generateSendMethod(target, protocolDescriptor, messageDescriptor);
            } else {
                generateReceiveMethod(target, protocolDescriptor, messageDescriptor);
            }
        }
    }

    private static void generateGeneralSendMethod(StringBuilder target) {
        target.append("\n\n    public void sendRequest(byte[] receiverId, byte[] messageType, IonCodec requestCodec) throws IOException {");
        target.append("\n        this.iapRequestBase.setReceiverNodeId(receiverId);");
        target.append("\n        this.iapRequestBase.setMessageType(messageType);");
        target.append("\n        TcpMessage outgoingMessage = this.tcpMessagePort.allocateWriteMemoryBlock(1024);");
        target.append("\n");
        target.append("\n        this.ionWriter.setDestination(outgoingMessage);");
        target.append("\n        this.ionWriter.writeObject(2, iapRequestBase, requestCodec, outgoingMessage);");
        target.append("\n        this.tcpMessagePort.writeNowOrEnqueue(this.serverTcpSocket, outgoingMessage);");
        target.append("\n        this.tcpMessagePort.writeBlock();");
        target.append("\n        outgoingMessage.free();");
        target.append("\n    }");
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

        target.append("\n        sendRequest(new byte[]{0}, ");
        appendStringRepOfBytes(target, messageDescriptor.messageType);
        target.append(", ");
        target.append(messageDescriptor.getMessageNameFirstCharLowercase());
        target.append(");");
        target.append("\n    }");
    }

    private static void generateGeneralReceiveMethod(StringBuilder target){
        target.append("\n\n    public void receiveResponse(IonCodec response) throws Exception{");
        target.append("\n        this.messageBatch.clear();");
        target.append("\n        this.tcpMessagePort.readBlock(this.messageBatch);");
        target.append("\n        this.ionReader.setSource(this.messageBatch.blocks[0]);");
        target.append("\n");
        target.append("\n        this.ionReader.nextParse().moveInto().nextParse();");
        target.append("\n        this.iapResponseBase.read(this.ionReader);");
        target.append("\n        ");
        target.append("\n        response.read(this.ionReader);");
        target.append("\n        ");
        target.append("\n        this.messageBatch.blocks[0].free();");
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

        target.append("\n        receiveResponse(");
        target.append(messageDescriptor.getMessageNameFirstCharLowercase());
        target.append(");");

        target.append("\n    }");

    }

    private static void appendFieldAsParameter(StringBuilder target, FieldDescriptor fieldDescriptor) {
        target.append(fieldDescriptor.getFieldType());
        target.append(" ");
        target.append(fieldDescriptor.fieldName);
    }
}
