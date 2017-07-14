package com.nanosai.gridops.codegen;

import com.nanosai.gridops.codegen.FieldDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * A description of a single message within an IAP semantic protocol. This description can be used to generate
 * a Java codec class that can read and write this message from bytes.
 *
 */
public class MessageDescriptor {

    public static final int UNKNOWN_MEP_TYPE = 0;
    public static final int REQUEST_MEP_TYPE = 1;
    public static final int RESPONSE_MEP_TYPE = 2;
    public static final int NOTIFICATION_OUT_MEP_TYPE = 3;
    public static final int NOTIFICATION_IN_MEP_TYPE = 4;


    public String messageName = null;
    public byte[] messageType = null;
    public int    mepType     = UNKNOWN_MEP_TYPE;

    public List<FieldDescriptor> fieldDescriptors = new ArrayList<>();

    public MessageDescriptor() {
    }

    public MessageDescriptor(String messageName, byte[] messageType, int mepType) {
        this.messageName = messageName;
        this.messageType = messageType;
        this.mepType     = mepType;
    }

    public FieldDescriptor addFieldDescriptor(String fieldName, int fieldType, byte[] fieldKey){
        FieldDescriptor fieldDescriptor = new FieldDescriptor(fieldName, fieldType, fieldKey);
        fieldDescriptors.add(fieldDescriptor);
        return fieldDescriptor;
    }

    public FieldDescriptor addFieldDescriptor(FieldDescriptor fieldDescriptor){
        fieldDescriptors.add(fieldDescriptor);
        return fieldDescriptor;
    }

    public String getMessageNameFirstCharUppercase() {
        return this.messageName.substring(0,1).toUpperCase() + this.messageName.substring(1);
    }

    public String getMessageNameFirstCharLowercase() {
        return this.messageName.substring(0,1).toLowerCase() + this.messageName.substring(1);
    }

    public boolean isRequest() {
        return this.mepType == REQUEST_MEP_TYPE;
    }

    public boolean isResponse() {
        return this.mepType == RESPONSE_MEP_TYPE;
    }

    public boolean isNotificationOut() {
        return this.mepType == NOTIFICATION_OUT_MEP_TYPE;
    }

    public boolean isNotificationIn() {
        return this.mepType == NOTIFICATION_IN_MEP_TYPE;
    }

}
