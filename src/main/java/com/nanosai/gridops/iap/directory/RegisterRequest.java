package com.nanosai.gridops.iap.directory;

import com.nanosai.gridops.iap.IIapMessageCodec;
import com.nanosai.gridops.iap.IapMessageKeys;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;

/**
 * Created by jjenkov on 26-11-2016.
 */
public class RegisterRequest implements IIapMessageCodec {

    public static final int ACCESS_TOKEN = 0;
    public static final int SERVICE_ID = 1;
    public static final int IP_ADDRESS = 2;
    public static final int TCP_PORT   = 3;


    private static final byte[] accessTokenKey = new byte[] {ACCESS_TOKEN};
    private static final byte[] serviceIdKey   = new byte[] {SERVICE_ID};
    private static final byte[] ipAddressKey   = new byte[] {IP_ADDRESS};
    private static final byte[] tcpPortKey     = new byte[] {TCP_PORT};



    // receiver system id
    public byte[] accessTokenSource = null;
    public int    accessTokenOffset = 0;
    public int    accessTokenLength = 0;

    // semantic protocol id
    public byte[] serviceIdSource = null;
    public int    serviceIdOffset = 0;
    public int    serviceIdLength = 0;

    // semantic protocol version
    public byte[] ipAddressSource = null;
    public int    ipAddressOffset = 0;
    public int    ipAddressLength = 0;

    // message type
    public int    tcpPort = 1111;


    public void setAccessToken(byte[] accessToken){
        this.accessTokenSource = accessToken;
        this.accessTokenOffset = 0;
        this.accessTokenLength = accessToken.length;
    }

    public void setServiceId(byte[] serviceId){
        this.serviceIdSource = serviceId;
        this.serviceIdOffset = 0;
        this.serviceIdLength = serviceId.length;
    }

    public void setIpAddress(byte[] ipAddress){
        this.ipAddressSource = ipAddress;
        this.ipAddressOffset = 0;
        this.ipAddressLength = ipAddress.length;
    }

    public void setTcpPort(int tcpPort){
        this.tcpPort = tcpPort;
    }


    @Override
    public void read(IonReader reader) {
        readAccessToken(reader);
        readServiceId(reader);
        readIpAddressVersion(reader);
        readTcpPort(reader);
    }

    private void readAccessToken(IonReader reader) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isIdKey(reader, ACCESS_TOKEN)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    this.accessTokenSource = reader.source;
                    this.accessTokenOffset = reader.index;
                    this.accessTokenLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after receiver system id.
            }
        }
    }

    private void readServiceId(IonReader reader) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isIdKey(reader, SERVICE_ID)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    this.serviceIdSource = reader.source;
                    this.serviceIdOffset = reader.index;
                    this.serviceIdLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after receiver system id.
            }
        }
    }

    private void readIpAddressVersion(IonReader reader) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isIdKey(reader, IP_ADDRESS)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.BYTES){
                    this.ipAddressSource = reader.source;
                    this.ipAddressOffset = reader.index;
                    this.ipAddressLength = reader.fieldLength;
                }
                reader.nextParse(); //move to next field after receiver system id.
            }
        }
    }

    private void readTcpPort(IonReader reader) {
        if(reader.fieldType == IonFieldTypes.KEY_SHORT){
            if(isIdKey(reader, IapMessageKeys.MESSAGE_TYPE)) {
                reader.nextParse();

                if(reader.fieldType == IonFieldTypes.INT_POS){
                    this.tcpPort = (int) reader.readInt64();
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
        writeAccessToken(writer);
        writeServiceId(writer);
        writeIpAddress(writer);
        writeTcpPort(writer);
    }

    public void writeAccessToken(IonWriter writer){
        writer.writeKeyShort(accessTokenKey);
        writer.writeBytes   (this.accessTokenSource, this.accessTokenOffset, this.accessTokenLength);
    }

    public void writeServiceId(IonWriter writer){
        writer.writeKeyShort(serviceIdKey);
        writer.writeBytes   (this.serviceIdSource, this.serviceIdOffset, this.serviceIdLength);
    }

    public void writeIpAddress(IonWriter writer){
        writer.writeKeyShort(ipAddressKey);
        writer.writeBytes   (this.ipAddressSource, this.ipAddressOffset, this.ipAddressLength);
    }

    public void writeTcpPort(IonWriter writer){
        writer.writeKeyShort(tcpPortKey);
        writer.writeInt64   (this.tcpPort);
    }

    public boolean equalsAccessToken(byte[] accessToken) {
        return equals(this.accessTokenSource, this.accessTokenOffset, this.accessTokenLength,
                      accessToken, 0, accessToken.length);
    }

    public boolean equalsServiceId(byte[] semanticProtocolId) {
        return equals(this.serviceIdSource, this.serviceIdOffset, this.serviceIdLength,
                      semanticProtocolId, 0, semanticProtocolId.length);
    }

    public boolean equalsIpAddress(byte[] ipAddress) {
        return equals(this.ipAddressSource, this.ipAddressOffset, this.ipAddressLength,
                ipAddress, 0, ipAddress.length);
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
