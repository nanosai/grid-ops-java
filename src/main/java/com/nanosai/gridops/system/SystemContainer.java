package com.nanosai.gridops.system;

import com.nanosai.gridops.iap.IapMessage;
import com.nanosai.gridops.ion.read.IonReader;

/**
 * Created by jjenkov on 22-09-2016.
 */
public class SystemContainer {

    private SystemReactor[] systemReactors = null;

    private IonReader reader         = new IonReader();

    public SystemContainer(SystemReactor... systemReactors) {
        this.systemReactors = systemReactors;
    }

    public void react(IonReader reader, IapMessage message) {
        if(message.receiverSystemIdLength > 0){
            SystemReactor systemReactor = findSystemReactor(message.data, message.receiverSystemIdOffset, message.receiverSystemIdLength);

            if(systemReactor != null){
                systemReactor.react(reader, message);
            }
        }
    }

    /**
     * Finds the message handler matching the given message type. If no message handler found
     * for the given message type, null is returned.
     *
     * @param systemId The message type to find the message handler for.
     * @return The message handler matching the given message type, or null if no message handler found.
     */
    public SystemReactor findSystemReactor(byte[] systemId, int offset, int length){
        for(int i = 0; i< systemReactors.length; i++){
            if(SystemUtil.equals(systemId, offset, length, systemReactors[i].systemId, 0, systemReactors[i].systemId.length)){
                return systemReactors[i];
            }
        }
        return null;
    }


}
