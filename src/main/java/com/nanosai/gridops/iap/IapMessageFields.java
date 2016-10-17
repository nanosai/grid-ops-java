package com.nanosai.gridops.iap;

/**
 * Created by jjenkov on 09-10-2016.
 */
public class IapMessageFields {

    //underlying byte array containing the data
    public byte[] data = null;

    // receiver system id
    public int receiverNodeIdOffset = 0;
    public int receiverNodeIdLength = 0;

    // semantic protocol id
    public int semanticProtocolIdOffset = 0;
    public int semanticProtocolIdLength = 0;

    // semantic protocol version
    public int semanticProtocolVersionOffset = 0;
    public int semanticProtocolVersionLength = 0;

    // message type
    public int messageTypeOffset = 0;
    public int messageTypeLength = 0;

    // message id




}
