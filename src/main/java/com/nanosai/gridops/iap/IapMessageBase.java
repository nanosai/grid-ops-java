package com.nanosai.gridops.iap;

import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;

/**
 * Created by jjenkov on 26-11-2016.
 */
public class IapMessageBase implements IIapMessageCodec {

    private static final byte[] receiverNodeIdKey              = new byte[] {IapMessageKeys.RECEIVER_NODE_ID};
    private static final byte[] receiverNodeIdCodeKey          = new byte[] {IapMessageKeys.RECEIVER_NODE_ID_CODE};
    private static final byte[] semanticProtocolIdKey          = new byte[] {IapMessageKeys.SEMANTIC_PROTOCOL_ID};
    private static final byte[] semanticProtocolIdCodeKey      = new byte[] {IapMessageKeys.SEMANTIC_PROTOCOL_ID_CODE};
    private static final byte[] semanticProtocolVersionKey     = new byte[] {IapMessageKeys.SEMANTIC_PROTOCOL_VERSION};
    private static final byte[] semanticProtocolVersionCodeKey = new byte[] {IapMessageKeys.SEMANTIC_PROTOCOL_VERSION_CODE};
    private static final byte[] messageTypeKey                 = new byte[] {IapMessageKeys.MESSAGE_TYPE};
    private static final byte[] messageTypeCodeKey             = new byte[] {IapMessageKeys.MESSAGE_TYPE_CODE};



    // receiver system id
    public byte[] receiverNodeIdSource = null;
    public int    receiverNodeIdOffset = 0;
    public int    receiverNodeIdLength = 0;

    // semantic protocol id
    public byte[] semanticProtocolIdSource = null;
    public int    semanticProtocolIdOffset = 0;
    public int    semanticProtocolIdLength = 0;

    // semantic protocol version
    public byte[] semanticProtocolVersionSource = null;
    public int    semanticProtocolVersionOffset = 0;
    public int    semanticProtocolVersionLength = 0;

    // message type
    public byte[] messageTypeSource = null;
    public int    messageTypeOffset = 0;
    public int    messageTypeLength = 0;


    public void setReceiverNodeId(byte[] receiverNodeId){
        this.receiverNodeIdSource = receiverNodeId;
        this.receiverNodeIdOffset = 0;
        this.receiverNodeIdLength = receiverNodeId.length;
    }

    public void setSemanticProtocolId(byte[] semanticProtocolId){
        this.semanticProtocolIdSource = semanticProtocolId;
        this.semanticProtocolIdOffset = 0;
        this.semanticProtocolIdLength = semanticProtocolId.length;
    }

    public void setSemanticProtocolVersion(byte[] semanticProtocolVersion){
        this.semanticProtocolVersionSource = semanticProtocolVersion;
        this.semanticProtocolVersionOffset = 0;
        this.semanticProtocolVersionLength = semanticProtocolVersion.length;
    }

    public void setMessageType(byte[] messageType){
        this.messageTypeSource = messageType;
        this.messageTypeOffset = 0;
        this.messageTypeLength = messageType.length;
    }


    @Override
    public void read(IonReader reader) {
        readReceiverNodeId(reader);
        readSemanticProtocolId(reader);
        readSemanticProtocolVersion(reader);
        readMessageType(reader);
    }

    private void readReceiverNodeId(IonReader reader) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isIdKey(reader, IapMessageKeys.RECEIVER_NODE_ID)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    this.receiverNodeIdSource = reader.source;
                    this.receiverNodeIdOffset = reader.index;
                    this.receiverNodeIdLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after receiver system id.
            }
        }
    }

    private void readSemanticProtocolId(IonReader reader) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isIdKey(reader, IapMessageKeys.SEMANTIC_PROTOCOL_ID)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    this.semanticProtocolIdSource = reader.source;
                    this.semanticProtocolIdOffset = reader.index;
                    this.semanticProtocolIdLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after receiver system id.
            }
        }
    }

    private void readSemanticProtocolVersion(IonReader reader) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isIdKey(reader, IapMessageKeys.SEMANTIC_PROTOCOL_VERSION)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    this.semanticProtocolVersionSource = reader.source;
                    this.semanticProtocolVersionOffset = reader.index;
                    this.semanticProtocolVersionLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after receiver system id.
            }
        }
    }

    private void readMessageType(IonReader reader) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isIdKey(reader, IapMessageKeys.MESSAGE_TYPE)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    this.messageTypeSource = reader.source;
                    this.messageTypeOffset = reader.index;
                    this.messageTypeLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after receiver system id.
            }
        }
    }

    private static boolean isIdKey(IonReader reader, int singleByteKeyValue){
        return reader.fieldLength == 1 && reader.source[reader.index] == singleByteKeyValue;
    }


    @Override
    public void write(IonWriter writer) {
        writeReceiverNodeId         (writer);
        writeSemanticProtocolId     (writer);
        writeSemanticProtocolVersion(writer);
        writeMessageType            (writer);
    }

    /*
    public static void writeReceiverNodeIdCode(IonWriter writer, int receiverSystemIdCode){
        writer.writeKeyShort(receiverNodeIdCodeKey);
        writer.writeInt64   (receiverSystemIdCode);
    }
    */

    public void writeReceiverNodeId(IonWriter writer){
        writer.writeKeyShort(receiverNodeIdKey);
        writer.writeBytes   (this.receiverNodeIdSource, this.receiverNodeIdOffset, this.receiverNodeIdLength);
    }

    /*
    public static void writeSemanticProtocolIdCode(IonWriter writer, int semanticProtocolIdCode){
        writer.writeKeyShort(semanticProtocolIdCodeKey);
        writer.writeInt64   (semanticProtocolIdCode);
    }
    */

    public void writeSemanticProtocolId(IonWriter writer){
        writer.writeKeyShort(semanticProtocolIdKey);
        writer.writeBytes   (this.semanticProtocolIdSource, this.semanticProtocolIdOffset, this.semanticProtocolIdLength);
    }

    /*
    public static void writeSemanticProtocolVersionCode(IonWriter writer, int semanticProtocolVersionCode){
        writer.writeKeyShort(semanticProtocolVersionCodeKey);
        writer.writeInt64   (semanticProtocolVersionCode);
    }
    */

    public void writeSemanticProtocolVersion(IonWriter writer){
        writer.writeKeyShort(semanticProtocolVersionKey);
        writer.writeBytes   (this.semanticProtocolVersionSource, this.semanticProtocolVersionOffset, this.semanticProtocolVersionLength);
    }

    /*
    public static void writeMessageTypeCode(IonWriter writer, int messageType){
        writer.writeKeyShort(messageTypeCodeKey);
        writer.writeInt64   (messageType);
    }
    */

    public void writeMessageType(IonWriter writer){
        writer.writeKeyShort(messageTypeKey);
        writer.writeBytes   (this.messageTypeSource, this.messageTypeOffset, this.messageTypeLength);
    }

    public boolean equalsReceiverNodeId(byte[] nodeId) {
        return equals(this.receiverNodeIdSource, this.receiverNodeIdOffset, this.receiverNodeIdLength,
                      nodeId, 0, nodeId.length);
    }

    public boolean equalsSemanticProtocolId(byte[] semanticProtocolId) {
        return equals(this.semanticProtocolIdSource, this.semanticProtocolIdOffset, this.semanticProtocolIdLength,
                      semanticProtocolId, 0, semanticProtocolId.length);
    }

    public boolean equalsSemanticProtocolVersion(byte[] semanticProtocolVersion) {
        return equals(this.semanticProtocolVersionSource, this.semanticProtocolVersionOffset, this.semanticProtocolVersionLength,
                semanticProtocolVersion, 0, semanticProtocolVersion.length);
    }

    public boolean equalsMessageType(byte[] messageType) {
        return equals(this.messageTypeSource, this.messageTypeOffset, this.messageTypeLength,
                messageType, 0, messageType.length);
    }

    public static boolean equals(byte[] source1, int offset1, int length1, byte[] source2, int offset2, int length2){
        if(length1 != length2){
            return false;
        }

        for(int i=0; i<length1; i++){
            if(source1[offset1 + i] != source2[offset2 + i]){
                return false;
            }
        }
        return true;
    }
}
