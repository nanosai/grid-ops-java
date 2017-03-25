package com.nanosai.gridops.ion.write;

import com.nanosai.gridops.ion.IonUtil;

import java.lang.reflect.Field;

/**
 * This class is a base class for IIonFieldWriter implementations.
 *
 */
public abstract class IonFieldWriterBase implements IIonFieldWriter {

    protected Field  field    = null;
    protected byte[] keyField = null;

    public IonFieldWriterBase(Field field, String alias) {
        this.field = field;
        this.keyField = IonUtil.preGenerateKeyField(alias);
    }

    @Override
    public int getKeyFieldLength() {
        return this.keyField.length;
    }

    @Override
    public int writeKeyField(byte[] destination, int destinationOffset) {
        System.arraycopy(this.keyField, 0, destination, destinationOffset, this.keyField.length);
        return this.keyField.length;
    }

    @Override
    public abstract int writeValueField(Object sourceObject, byte[] destination, int destinationOffset, int maxLengthLength);
}
