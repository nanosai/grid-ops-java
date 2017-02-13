package com.nanosai.gridops.iap.error;

import com.nanosai.gridops.iap.IapMessageKeys;
import com.nanosai.gridops.iap.IapSemanticProtocolIds;

/**
 * Created by jjenkov on 14/01/2017.
 */
public class ErrorMessageConstants {

    public static final byte[] errorCodeSemanticProtocolId = new byte[] {IapSemanticProtocolIds.ERROR_PROTOCOL_ID};
    public static final byte[] semanticProtocolVersion     = new byte[] {0};
    public static final byte[] errorResponseMessageType    = new byte[] {0};

    public static final byte[] errorIdNoError                = new byte[] {0};
    public static final byte[] errorIdUnknownNodeId          = new byte[] {1};
    public static final byte[] errorIdUnsupportedProtocol    = new byte[] {2};
    public static final byte[] errorIdUnsupportedMessageType = new byte[] {3};



}
