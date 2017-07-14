package com.nanosai.gridops.codegen;

import com.nanosai.gridops.codegen.MessageDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * A description of an IAP semantic protocol which can be used to generate Java classes for reading and writing
 * messages of that protocol.
 */
public class SemanticProtocolDescriptor {
    public String semanticProtocolName    = null;
    public byte[] semanticProtocolId      = null;
    public byte[] semanticProtocolVersion = null;

    public List<MessageDescriptor> messageDescriptors = new ArrayList<>();

    public SemanticProtocolDescriptor() {
    }

    public SemanticProtocolDescriptor(String semanticProtocolName, byte[] semanticProtocolId, byte[] semanticProtocolVersion) {
        this.semanticProtocolName = semanticProtocolName;
        this.semanticProtocolId = semanticProtocolId;
        this.semanticProtocolVersion = semanticProtocolVersion;
    }

    public void addMessageDescriptor(MessageDescriptor messageDescriptor){
        this.messageDescriptors.add(messageDescriptor);
    }

    public MessageDescriptor addMessageDescriptor(String messageName, byte[] messageType, int mepType){
        MessageDescriptor messageDescriptor = new MessageDescriptor(messageName, messageType, mepType);
        this.messageDescriptors.add(messageDescriptor);
        return messageDescriptor;
    }
}
