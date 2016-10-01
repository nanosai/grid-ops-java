package com.nanosai.gridops.iap;

import com.nanosai.gridops.ion.write.IonWriter;

/**
 * Created by jjenkov on 30-09-2016.
 */
public class IapErrorMessageWriter {

    byte[] semanticProtocolIdKeyValue      = new byte[] {IapMessageKeys.SEMANTIC_PROTOCOL_ID_KEY_VALUE};
    byte[] semanticProtocolVersionKeyValue = new byte[] {IapMessageKeys.SEMANTIC_PROTOCOL_VERSION_KEY_VALUE};
    byte[] messageTypeKeyValue             = new byte[] {IapMessageKeys.MESSAGE_TYPE_KEY_VALUE};

    byte[] errorCodeKeyValue               = new byte[] {10};
    byte[] errorMessageKeyValue            = new byte[] {11};

    IonWriter writer = null;

    public IapErrorMessageWriter() {
        this.writer = new IonWriter();
        this.writer.setComplexFieldStack(new int[2]);

    }

    public void writeErrorMessage(long errorCode, String errorMessage){

        writer.writeObjectBeginPush(2);

        // protocol id
        writer.writeKeyShort(semanticProtocolIdKeyValue);

        // protocol version
        writer.writeKeyShort(semanticProtocolVersionKeyValue);

        // message type
        writer.writeKeyShort(messageTypeKeyValue);

        // error code


        // error message


        writer.writeObjectEndPop();

    }
}
