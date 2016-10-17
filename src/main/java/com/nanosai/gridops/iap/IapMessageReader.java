package com.nanosai.gridops.iap;

import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;

/**
 * Created by jjenkov on 09-10-2016.
 */
public class IapMessageReader {


    public static void read(IonReader reader, IapMessageFields message) {

        // assume IonReader is already pointing to the first ION field inside the ION Object field

        // read receiver node id
        readReceiverNodeId(reader, message);

        // read semantic protocol id
        readSemanticProtocolId(reader, message);

        // read semantic protocol version
        readSemanticProtocolVersion(reader, message);

        // read message type
        readMessageType(reader, message);


        // read message id



    }

    /**
     * Reads the receiver system id from the IonReader - if present (is optional).
     *
     * @param reader
     * @param message
     */
    private static void readReceiverNodeId(IonReader reader, IapMessageFields message) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isSystemIdKey(reader)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    message.receiverNodeIdOffset = reader.index;
                    message.receiverNodeIdLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after receiver system id.
            }
        }
    }

    /**
     * Reads the semantic protocol id from the IonReader - if present (is optional).
     *
     * @param reader
     * @param message
     */
    private static void readSemanticProtocolId(IonReader reader, IapMessageFields message) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isSemanticProtoocolIdKey(reader)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    message.semanticProtocolIdOffset = reader.index;
                    message.semanticProtocolIdLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after semantic protocol id.
            }
        }
    }

    /**
     * Reads the semantic protocol version from the IonReader - if present (is optional).
     *
     * @param reader
     * @param message
     */
    private static void readSemanticProtocolVersion(IonReader reader, IapMessageFields message) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isSemanticProtoocolVersionKey(reader)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    message.semanticProtocolVersionOffset = reader.index;
                    message.semanticProtocolVersionLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after semantic protocol version.
            }
        }
    }

    /**
     * Reads the message type from the IonReader - if present (is optional).
     *
     * @param reader
     * @param message
     */
    private static void readMessageType(IonReader reader, IapMessageFields message) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isMessageTypeKey(reader)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    message.messageTypeOffset = reader.index;
                    message.messageTypeLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after message type version.
            }
        }
    }

    private static boolean isSystemIdKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.RECEIVER_NODE_ID;
    }

    private static boolean isSystemIdCodeKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.RECEIVER_NODE_ID_CODE;
    }

    private static boolean isSemanticProtoocolIdKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.SEMANTIC_PROTOCOL_ID;
    }

    private static boolean isSemanticProtoocolIdCodeKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.SEMANTIC_PROTOCOL_ID_CODE;
    }

    private static boolean isSemanticProtoocolVersionKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.SEMANTIC_PROTOCOL_VERSION;
    }

    private static boolean isSemanticProtoocolVersionCodeKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.SEMANTIC_PROTOCOL_VERSION_CODE;
    }

    private static boolean isMessageTypeKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.MESSAGE_TYPE;
    }

    private static boolean isMessageTypeCodeKey(IonReader reader) {
        return reader.fieldLength == 1 && reader.source[reader.index] == IapMessageKeys.MESSAGE_TYPE_CODE;
    }


}
