package com.nanosai.gridops.ion.write;

import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.IonUtil;

import java.lang.reflect.Field;

/**
 * Created by jjenkov on 04-11-2015.
 */
public class IonFieldWriterFloat extends IonFieldWriterBase implements IIonFieldWriter {


    public IonFieldWriterFloat(Field field, String alias) {
        super(field, alias);
    }


    @Override
    public int writeValueField(Object sourceObject, byte[] dest, int destOffset, int maxLengthLength) {
        try {
            float value = (float) field.get(sourceObject);
            int valueIntBits = Float.floatToIntBits(value);

            //magic number "4" is the length in bytes of a 32 bit floating point number in ION.

            dest[destOffset++] = (byte) (255 & ((IonFieldTypes.FLOAT << 4) | 4));

            for(int i=(4-1)*8; i >= 0; i-=8){
                dest[destOffset++] = (byte) (255 & (valueIntBits >> i));
            }

            return 5;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
