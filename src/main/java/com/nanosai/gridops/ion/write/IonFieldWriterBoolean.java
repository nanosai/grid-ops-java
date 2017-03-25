package com.nanosai.gridops.ion.write;

import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.IonUtil;

import java.lang.reflect.Field;

/**
 * Created by jjenkov on 04-11-2015.
 */
public class IonFieldWriterBoolean extends IonFieldWriterBase implements IIonFieldWriter {

    public IonFieldWriterBoolean(Field field, String alias) {
        super(field, alias);
    }

    @Override
    public int writeValueField(Object sourceObject, byte[] destination, int destinationOffset, int maxLengthLength) {
        try {
            boolean value = (Boolean) field.get(sourceObject);

            if(value){
                destination[destinationOffset] = (byte) (255 & ((IonFieldTypes.BOOLEAN << 4) | 1));
            } else {
                destination[destinationOffset] = (byte) (255 & ((IonFieldTypes.BOOLEAN << 4) | 2));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return 1;    //total length of a boolean field is always 1
    }


 }
