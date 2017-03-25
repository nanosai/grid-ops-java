package com.nanosai.gridops.ion.write;

/**
 * Created by jjenkov on 04-11-2015.
 */
public interface IIonFieldWriter {

    public int getKeyFieldLength();
    public int writeKeyField  (byte[] destination, int destinationOffset);
    public int writeValueField(Object sourceObject, byte[] destination, int destinationOffset, int maxLengthLength);


}
