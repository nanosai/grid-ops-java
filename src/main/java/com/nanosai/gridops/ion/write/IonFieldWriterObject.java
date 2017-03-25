package com.nanosai.gridops.ion.write;

import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.IonUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jjenkov on 04-11-2015.
 */
public class IonFieldWriterObject extends IonFieldWriterBase implements IIonFieldWriter {

    public Field[] fields    = null;

    public IIonFieldWriter[] fieldWriters = null;

    public IonFieldWriterObject(Field field, String alias) {
        super(field, alias);
    }

    public void generateFieldWriters(IIonObjectWriterConfigurator configurator, Map<Field, IIonFieldWriter> existingFieldWriters) {
        //generate field writers for this IonFieldWriterObject instance - fields in the class of this field.
        this.fieldWriters = IonUtil.createFieldWriters(field.getType().getDeclaredFields(), configurator, existingFieldWriters);
    }


    @Override
    public int writeValueField(Object sourceObject, byte[] destination, int destinationOffset, int maxLengthLength) {

        try {
            Object fieldValue = this.field.get(sourceObject);
            if(fieldValue == null){
                destination[destinationOffset++] = (byte) (255 & ((IonFieldTypes.OBJECT << 4) | 0)); //marks a null with 0 lengthLength
                return 1;
            }

            destination[destinationOffset++] = (byte) (255 & ((IonFieldTypes.OBJECT << 4) | maxLengthLength));

            int lengthOffset   = destinationOffset; //store length start offset for later use
            destinationOffset += maxLengthLength;


            for(int i=0; i<fieldWriters.length; i++){
                if(fieldWriters[i] != null){
                    destinationOffset += fieldWriters[i].writeKeyField(destination, destinationOffset);
                    destinationOffset += fieldWriters[i].writeValueField(fieldValue, destination, destinationOffset, maxLengthLength);
                }
            }

            int fullFieldLength   = destinationOffset - (lengthOffset + maxLengthLength);

            switch(maxLengthLength){
                case 4 : destination[lengthOffset++] = (byte) (255 & (fullFieldLength >> 24));
                case 3 : destination[lengthOffset++] = (byte) (255 & (fullFieldLength >> 16));
                case 2 : destination[lengthOffset++] = (byte) (255 & (fullFieldLength >>  8));
                case 1 : destination[lengthOffset++] = (byte) (255 & (fullFieldLength));
            }

            return 1 + maxLengthLength + fullFieldLength;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            //todo should never happen, as we set all Field instances to accessible.
        }

        return 0;
    }

}
