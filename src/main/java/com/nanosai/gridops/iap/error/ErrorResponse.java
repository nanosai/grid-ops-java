package com.nanosai.gridops.iap.error;

import com.nanosai.gridops.iap.IapSemanticProtocolIds;
import com.nanosai.gridops.ion.write.IonWriter;

/**
 * This class represents an error response for some request that could not be processed successfully.
 * You can create an ErrorResponse and reuse it because it is not stateful.
 */
public class ErrorResponse {

    private static final byte[] errorCodeSemanticProtocolId     = new byte[] {IapSemanticProtocolIds.ERROR_PROTOCOL_ID};

    public static final byte ERROR_ID_KEY  = -1;
    public static final byte ERROR_MESSAGE_KEY = -2;

    private static final byte[] errorIdKeyBytes      = new byte[]{ERROR_ID_KEY};
    private static final byte[] errorMessageKeyBytes = new byte[]{ERROR_MESSAGE_KEY};


    public static void writeErrorMessageFields(IonWriter writer, byte[] errorId, String errorMessage){
        writeErrorId(writer, errorId);
        writeErrorMessage(writer, errorMessage);
    }

    public static void writeErrorId(IonWriter writer, byte[] errorId){
        writer.writeKeyShort(errorIdKeyBytes);
        writer.writeBytes(errorId);
    }

    public static void writeErrorMessage(IonWriter writer, String errorMessage){
        writer.writeKeyShort(errorMessageKeyBytes);
        writer.writeUtf8(errorMessage);
    }
}
