package com.nanosai.gridops.ion.write;

/**
 * Created by jjenkov on 10-02-2016.
 */
public class IonObjectWriterConfiguratorNopImpl implements IIonObjectWriterConfigurator {

    public static final IonObjectWriterConfiguratorNopImpl DEFAULT_INSTANCE = new IonObjectWriterConfiguratorNopImpl();

    @Override
    public void configure(IonFieldWriterConfiguration config) {
        //do nothing.
    }
}
