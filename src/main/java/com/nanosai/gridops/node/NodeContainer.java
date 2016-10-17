package com.nanosai.gridops.node;

import com.nanosai.gridops.iap.IapMessageFields;
import com.nanosai.gridops.ion.read.IonReader;
import com.nanosai.gridops.tcp.TcpSocketsPort;

/**
 * Created by jjenkov on 22-09-2016.
 */
public class NodeContainer {

    private NodeReactor[] nodeReactors = null;


    public NodeContainer(NodeReactor... nodeReactors) {
        this.nodeReactors = nodeReactors;
    }

    public void react(IonReader reader, IapMessageFields message, TcpSocketsPort tcpSocketsPort) {
        if(message.receiverNodeIdLength > 0){
            NodeReactor nodeReactor = findNodeReactor(message.data, message.receiverNodeIdOffset, message.receiverNodeIdLength);

            if(nodeReactor != null){
                nodeReactor.react(reader, message, tcpSocketsPort);
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
    public NodeReactor findNodeReactor(byte[] systemId, int offset, int length){
        for(int i = 0; i< nodeReactors.length; i++){
            if(NodeUtil.equals(systemId, offset, length, nodeReactors[i].systemId, 0, nodeReactors[i].systemId.length)){
                return nodeReactors[i];
            }
        }
        return null;
    }


}
