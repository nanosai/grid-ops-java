package com.nanosai.gridops.codegen;

import com.nanosai.gridops.ion.IonFieldTypes;

/**
 * A description of a single field in a single type of message in an IAP semantic protocol.
 *
 */
public class FieldDescriptor {
    public String fieldName     = null;
    public int    fieldType     = -1;       //coming from IonFieldTypes constants
    public byte[] fieldKeyValue = null;

    public FieldDescriptor() {
    }

    public FieldDescriptor(String fieldName, int fieldType, byte[] fieldKeyValue) {
        this.fieldName     = fieldName;
        this.fieldType     = fieldType;
        this.fieldKeyValue = fieldKeyValue;
    }

    public String getFieldType() {
        if(IonFieldTypes.BYTES == fieldType) { return "byte[]"; }
        if(IonFieldTypes.BOOLEAN == fieldType) { return "boolean"; }
        if(IonFieldTypes.INT_POS == fieldType) { return "long"; }
        if(IonFieldTypes.INT_NEG == fieldType) { return "long"; }
        if(IonFieldTypes.FLOAT == fieldType) { return "double"; }
        if(IonFieldTypes.UTF_8 == fieldType) { return "String"; }
        if(IonFieldTypes.UTF_8_SHORT == fieldType) { return "String"; }
        if(IonFieldTypes.UTC_DATE_TIME == fieldType) { return "Calendar"; }
        if(IonFieldTypes.ARRAY == fieldType) { return "byte[]"; }
        if(IonFieldTypes.TABLE == fieldType) { return "byte[]"; }
        if(IonFieldTypes.OBJECT == fieldType) { return "byte[]"; }
        return "byte[]";
    }

    public boolean isByteArrayType(){
        if(IonFieldTypes.BYTES == fieldType) { return true; }
        if(IonFieldTypes.UTF_8 == fieldType) { return true; }
        if(IonFieldTypes.UTF_8_SHORT == fieldType) { return true; }
        if(IonFieldTypes.ARRAY == fieldType) { return true; }
        if(IonFieldTypes.TABLE == fieldType) { return true; }
        if(IonFieldTypes.OBJECT == fieldType) { return true; }

        return false;
    }

    public String getFieldNameFirstCharUppercase() {
        return this.fieldName.substring(0,1).toUpperCase() + this.fieldName.substring(1);
    }

    public String getFieldNameFirstCharLowercase() {
        return this.fieldName.substring(0,1).toLowerCase() + this.fieldName.substring(1);
    }





}
