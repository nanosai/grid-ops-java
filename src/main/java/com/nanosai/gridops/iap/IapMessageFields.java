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


    public boolean equalsReceiverNodeId(byte[] receiverNodeId){
        return equals(this.data, this.receiverNodeIdOffset, this.receiverNodeIdLength, receiverNodeId);
    }

    public boolean equalsSemanticProtocolId(byte[] semanticProtocolId){
        return equals(this.data, this.semanticProtocolIdOffset, this.semanticProtocolIdLength, semanticProtocolId);
    }

    public boolean equalsSemanticProtocolVersion(byte[] semanticProtocolVersion){
        return equals(this.data, this.semanticProtocolVersionOffset, this.semanticProtocolVersionLength, semanticProtocolVersion);
    }

    public boolean equalsMessageType(byte[] messageType){
        return equals(this.data, this.messageTypeOffset, this.messageTypeLength, messageType);
    }

    private static boolean equals(byte[] data1, int data1Offset, int data1Length, byte[] data2){
        if(data1Length != data2.length){
            return false;
        }

        for(int i=0; i < data1Length; i++){
            if(data1[data1Offset + i] != data2[i]){
                return false;
            }
        }
        return true;
    }


}
