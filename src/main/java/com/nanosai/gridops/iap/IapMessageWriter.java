package com.nanosai.gridops.iap;

import com.nanosai.gridops.iap.error.ErrorMessageTypes;
import com.nanosai.gridops.ion.write.IonWriter;

/**
 * Created by jjenkov on 02-10-2016.
 */
public class IapMessageWriter {

    private static final byte[] semanticProtocolIdKeyValue      = new byte[] {IapMessageKeys.SEMANTIC_PROTOCOL_ID_KEY_VALUE};
    private static final byte[] semanticProtocolVersionKeyValue = new byte[] {IapMessageKeys.SEMANTIC_PROTOCOL_VERSION_KEY_VALUE};
    private static final byte[] messageTypeKeyValue             = new byte[] {IapMessageKeys.MESSAGE_TYPE_KEY_VALUE};



    public static void writeStandardHeaders(IonWriter writer, byte[] semanticProtocolId, byte[] semanticProtocolVersion, int messageType ) {

        // protocol id
        writer.writeKeyShort(semanticProtocolIdKeyValue);
        writer.writeBytes   (semanticProtocolId);

        // protocol version
        writer.writeKeyShort(semanticProtocolVersionKeyValue);
        writer.writeBytes   (semanticProtocolVersion);

        // message type
        writer.writeKeyShort(messageTypeKeyValue);
        writer.writeInt64   (messageType);


    }
}
