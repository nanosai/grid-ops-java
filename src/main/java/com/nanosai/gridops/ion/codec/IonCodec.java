package com.nanosai.gridops.ion.codec;

import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;

/**
 * An IonCodec implementation is capable of reading and writing some data structure to and from the ION data format.
 * Such implementations are often simple value objects representing full or partial IAP messages.
 *
 */
public interface IonCodec {

    public void read(IonReader reader);
    public void write(IonWriter writer);


}
