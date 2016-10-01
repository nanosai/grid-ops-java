package com.nanosai.gridops.iap.error;

import com.nanosai.gridops.iap.IapMessageKeys;
import com.nanosai.gridops.iap.IapSemanticProtocolIds;
import com.nanosai.gridops.ion.write.IonWriter;
import com.nanosai.gridops.mem.MemoryBlock;

/**
 * Created by jjenkov on 30-09-2016.
 */
public class ErrorMessageWriter {

    private static final byte[] semanticProtocolIdKeyValue      = new byte[] {IapMessageKeys.SEMANTIC_PROTOCOL_ID_KEY_VALUE};
    private static final byte[] errorCodeSemanticProtocolId     = new byte[] {IapSemanticProtocolIds.ERROR_PROTOCOL_ID};

    private static final byte[] semanticProtocolVersionKeyValue = new byte[] {IapMessageKeys.SEMANTIC_PROTOCOL_VERSION_KEY_VALUE};
    private static final byte[] semanticProtocolVersion         = new byte[] {0};

    private static final byte[] messageTypeKeyValue             = new byte[] {IapMessageKeys.MESSAGE_TYPE_KEY_VALUE};

    private static final byte[] errorCodeKeyValue               = new byte[] {10};
    private static final byte[] errorMessageKeyValue            = new byte[] {11};


    IonWriter writer = null;

    public ErrorMessageWriter() {
        this.writer = new IonWriter();
        this.writer.setComplexFieldStack(new int[2]);

    }

    public void writeErrorMessage(MemoryBlock memoryBlock, long errorCode, String errorMessage){

        writer.setDestination(memoryBlock);
        writer.writeObjectBeginPush(2);

        // protocol id
        writer.writeKeyShort(semanticProtocolIdKeyValue);
        writer.writeBytes   (errorCodeSemanticProtocolId);

        // protocol version
        writer.writeKeyShort(semanticProtocolVersionKeyValue);
        writer.writeBytes   (semanticProtocolVersion);

        // message type
        writer.writeKeyShort(messageTypeKeyValue);
        writer.writeInt64   (ErrorMessageTypes.ERROR_MESSAGE_TYPE);

        // error code
        writer.writeKeyShort(errorCodeKeyValue);
        writer.writeInt64   (errorCode);

        // error message
        writer.writeKeyShort(errorMessageKeyValue);
        writer.writeUtf8    (errorMessage);

        writer.writeObjectEndPop();

    }
}
