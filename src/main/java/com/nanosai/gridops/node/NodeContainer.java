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
            NodeReactor nodeReactor = findNodeReactor(message);

            if(nodeReactor != null){
                nodeReactor.react(reader, message, tcpSocketsPort);
            }
        }
    }

    /**
     * Finds the message handler matching the given message type. If no message handler found
     * for the given message type, null is returned.
     *
     * @param messageFields The message fields containing the receiver node id to find the node reactor for.
     * @return The node reactor matching the given receiver node id, or null if no node reactor found.
     */
    public NodeReactor findNodeReactor(IapMessageFields messageFields){
        for(int i = 0; i< nodeReactors.length; i++){
            if(messageFields.equalsReceiverNodeId( this.nodeReactors[i].nodeId)){
                return nodeReactors[i];
            }
        }
        return null;
    }


}
