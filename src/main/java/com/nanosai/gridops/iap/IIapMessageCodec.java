package com.nanosai.gridops.iap;

import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.ion.write.IonWriter;

/**
 * Created by jjenkov on 26-11-2016.
 */
public interface IIapMessageCodec {

    public void read(IonReader reader);
    public void write(IonWriter writer);


}
