package com.nanosai.gridops.ion.write;

import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.IonUtil;

import java.lang.reflect.Field;

/**
 * Created by jjenkov on 04-11-2015.
 */
public class IonFieldWriterLong implements IIonFieldWriter {

    protected Field  field    = null;
    protected byte[] keyField = null;

    public IonFieldWriterLong(Field field, String alias) {
        this.field = field;
        this.keyField = IonUtil.preGenerateKeyField(alias);
    }


    @Override
    public int writeKeyAndValueFields(Object sourceObject, byte[] destination, int destinationOffset, int maxLengthLength) {

        System.arraycopy(this.keyField, 0, destination, destinationOffset, this.keyField.length);
        destinationOffset += this.keyField.length;

        return this.keyField.length + writeValueField(sourceObject, destination, destinationOffset, maxLengthLength);
    }

    @Override
    public int writeValueField(Object sourceObject, byte[] dest, int destOffset, int maxLengthLength) {
        try {
            long value = (long) field.get(sourceObject);
            int ionFieldType = IonFieldTypes.INT_POS;
            if(value < 0){
                ionFieldType = IonFieldTypes.INT_NEG;
                value  = -(value+1);
            }

            int length = IonUtil.lengthOfInt64Value(value);

            dest[destOffset++] = (byte) (255 & ((ionFieldType << 4) | length));

            for(int i=(length-1)*8; i >= 0; i-=8){
                dest[destOffset++] = (byte) (255 & (value >> i));
            }

            return 1 + length;

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
