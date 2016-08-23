package com.nanosai.gridops.ion.read;

import com.nanosai.gridops.ion.write.IIonObjectWriterConfigurator;
import com.nanosai.gridops.ion.write.IonFieldWriterConfiguration;

/**
 * Created by jjenkov on 10-02-2016.
 */
public class IonObjectReaderConfiguratorNopImpl implements IIonObjectReaderConfigurator {

    public static final IonObjectReaderConfiguratorNopImpl DEFAULT_INSTANCE = new IonObjectReaderConfiguratorNopImpl();

    @Override
    public void configure(IonFieldReaderConfiguration config) {
        //do nothing.
    }
}
