package com.nanosai.gridops.ion.write;

import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.IonUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

/**
 * Created by jjenkov on 04-11-2015.
 */
public class IonFieldWriterArrayByte extends IonFieldWriterBase implements IIonFieldWriter {

    public IonFieldWriterArrayByte(Field field, String alias) {
        super(field, alias);
    }

    @Override
    public int writeValueField(Object sourceObject, byte[] dest, int destOffset, int maxLengthLength) {
        try {
            byte[] value = (byte[]) field.get(sourceObject);

            if(value == null) {
                dest[destOffset++] = (byte) (255 & ((IonFieldTypes.BYTES << 4))); //byte array which is null
                return 1;
            }

            int length = value.length;

            int lengthLength = IonUtil.lengthOfInt64Value(length);
            dest[destOffset++] = (byte) (255 & ((IonFieldTypes.BYTES << 4) | lengthLength) );

            for(int i=(lengthLength-1)*8; i >= 0; i-=8){
                dest[destOffset++] = (byte) (255 & (length >> i));
            }

            System.arraycopy(value, 0, dest, destOffset, value.length);

            return 1 + lengthLength + length; //total length of a UTF-8 field
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
