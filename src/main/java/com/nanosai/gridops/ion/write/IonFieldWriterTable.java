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
public class IonFieldWriterTable extends IonFieldWriterBase implements IIonFieldWriter {

    protected byte[] allKeyFieldBytes = null;
    protected IIonFieldWriter[] fieldWritersForArrayType = null;


    public IonFieldWriterTable(Field field, String alias) {
        super(field, alias);
    }

    public void generateFieldWriters(IIonObjectWriterConfigurator configurator, Map<Field, IIonFieldWriter> existingFieldWriters) {
        this.fieldWritersForArrayType = IonUtil.createFieldWriters(
                this.field.getType().getComponentType().getDeclaredFields(), configurator, existingFieldWriters);

        preGenerateAllKeyFields();
    }

    private void preGenerateAllKeyFields() {
        int totalKeyFieldsLength = 0;

        for(int i=0; i < this.fieldWritersForArrayType.length; i++){
            totalKeyFieldsLength += this.fieldWritersForArrayType[i].getKeyFieldLength();
        }

        allKeyFieldBytes = new byte[totalKeyFieldsLength];

        int offset = 0;
        for(int i=0; i < this.fieldWritersForArrayType.length; i++){
            offset += this.fieldWritersForArrayType[i].writeKeyField(allKeyFieldBytes, offset);
        }

    }


    @Override
    public int writeValueField(Object sourceObject, byte[] destination, int destinationOffset, int maxLengthLength) {

        try {
            Object array = (Object) field.get(sourceObject);

            if(array == null) {
                destination[destinationOffset++] = (byte) (255 & ((IonFieldTypes.TABLE << 4) | 0)); //marks a null with 0 lengthLength
                return 1;
            }
            int startIndex = destinationOffset;
            destination[destinationOffset] = (byte) (255 & (IonFieldTypes.TABLE << 4) | (maxLengthLength));
            destinationOffset += 1 + maxLengthLength ; // 1 for lead byte + make space for maxLengthLength length bytes.


            System.arraycopy(this.allKeyFieldBytes, 0, destination, destinationOffset, this.allKeyFieldBytes.length);
            destinationOffset += this.allKeyFieldBytes.length;


            int arrayLength = Array.getLength(array);
            for(int i=0; i<arrayLength; i++){
                Object source = Array.get(array, i);

                //for each field in source write its field value out.
                for(int j=0; j < this.fieldWritersForArrayType.length; j++){
                    destinationOffset += this.fieldWritersForArrayType[j].writeValueField(source, destination, destinationOffset, maxLengthLength);
                }
            }

            int valueLength = destinationOffset - startIndex;

            IonUtil.writeLength(valueLength - 1 - maxLengthLength, maxLengthLength, destination, startIndex + 1);

            return valueLength;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
