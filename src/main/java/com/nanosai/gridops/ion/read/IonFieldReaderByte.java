package com.nanosai.gridops.ion.read;

import com.nanosai.gridops.ion.IonFieldTypes;

import java.lang.reflect.Field;

/**
 * Created by jjenkov on 05-11-2015.
 */
public class IonFieldReaderByte implements IIonFieldReader {

    private Field field = null;

    public IonFieldReaderByte(Field field) {
        this.field = field;
    }

    @Override
    public int read(byte[] source, int sourceOffset, Object destination) {
        int leadByte     = 255 & source[sourceOffset++];
        //int fieldType    = leadByte >> 3;  //todo use field type for validation ?
        int length = leadByte & 15;

        if(length == 0){
            return 1; //byte field with null value is always 1 byte long.
        }

        byte theByte = (byte) (255 & source[sourceOffset++]);
        for(int i=1;i<length; i++){
            theByte <<= 8;
            theByte |= 255 & source[sourceOffset++];
        }
        if( (leadByte >> 4) == IonFieldTypes.INT_NEG){
            theByte = (byte) ((-theByte) - 1);
        }


        try {
            field.set(destination, theByte);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return 1 + length;
    }

    @Override
    public void setNull(Object destination) {
        try {
            field.set(destination, null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
