package com.nanosai.gridops.ion.write;

import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.IonUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

/**
 * Created by jjenkov on 04-11-2015.
 */
public class IonFieldWriterString extends IonFieldWriterBase implements IIonFieldWriter {

    public IonFieldWriterString(Field field, String alias) {
        super(field, alias);
    }

    @Override
    public int writeValueField(Object sourceObject, byte[] dest, int destOffset, int maxLengthLength) {
        try {
            String value = (String) field.get(sourceObject);

            //todo optimize this - do not get bytes from a string like this. UTF-8 encode char-for-char with charAt() instead.
            byte[] valueBytes = value.getBytes("UTF-8");

            int length = valueBytes.length;

            if(length <= 15){
                dest[destOffset++] = (byte) (255 & ((IonFieldTypes.UTF_8_SHORT << 4) | length) );
                System.arraycopy(valueBytes, 0, dest, destOffset, valueBytes.length);

                return 1 + length;
            } else {
                int lengthLength = IonUtil.lengthOfInt64Value(length);
                dest[destOffset++] = (byte) (255 & ((IonFieldTypes.UTF_8 << 4) | lengthLength) );

                for(int i=(lengthLength-1)*8; i >= 0; i-=8){
                    dest[destOffset++] = (byte) (255 & (length >> i));
                }

                System.arraycopy(valueBytes, 0, dest, destOffset, valueBytes.length);

                return 1 + lengthLength + length; //total length of a UTF-8 field
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            //will never happen - UTF-8 always supported
        }
        return 0;
    }
}
