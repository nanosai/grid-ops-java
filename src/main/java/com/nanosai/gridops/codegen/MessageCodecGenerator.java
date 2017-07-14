package com.nanosai.gridops.codegen;

import com.nanosai.gridops.ion.IonFieldTypes;

/**
 * Generates codec classes from a SemanticProtocolDescriptor or MessageDescriptor. The codec classes
 * implement the IonCodec interface.
 */
public class MessageCodecGenerator {

    public static void main(String[] args) {
        MessageDescriptor messageDescriptor = new MessageDescriptor("CreateAccountRequest", new byte[]{0}, MessageDescriptor.REQUEST_MEP_TYPE);

        messageDescriptor.addFieldDescriptor("email", IonFieldTypes.UTF_8, new byte[]{16});
        messageDescriptor.addFieldDescriptor("password", IonFieldTypes.UTF_8, new byte[]{17});
        messageDescriptor.addFieldDescriptor("checksum", IonFieldTypes.INT_POS, new byte[]{18});

        StringBuilder builder = new StringBuilder();
        generate(builder, true, messageDescriptor);

        System.out.println(builder);
    }


    public static StringBuilder generate(StringBuilder target, boolean isInnerClass, MessageDescriptor messageDescriptor) {
        generateImports(target);

        target.append("\npublic ");
        if (isInnerClass) {
            target.append("static ");
        }
        target.append("class ");
        target.append(messageDescriptor.messageName);
        target.append(" implements IonCodec {");

        generateKeyValueConstants(target, messageDescriptor);
        target.append("\n");
        generateFields(target, messageDescriptor);
        generateGetters(target, messageDescriptor);
        generateSetters(target, messageDescriptor);
        generateReadMethods(target, messageDescriptor);
        target.append("\n");
        generateWriteMethod(target, messageDescriptor);

        target.append("\n}\n");

        return target;
    }


    private static void generateImports(StringBuilder target) {
        target.append("\nimport com.nanosai.gridops.ion.codec.IonCodec;");
        target.append("\nimport com.nanosai.gridops.ion.read.IonReader;");
        target.append("\nimport com.nanosai.gridops.ion.IonFieldTypes;");
        target.append("\nimport com.nanosai.gridops.ion.write.IonWriter;");
        target.append("\n");
        target.append("\nimport java.io.UnsupportedEncodingException;");
        target.append("\n");
    }

    private static void generateKeyValueConstants(StringBuilder target, MessageDescriptor messageDescriptor) {
        for (FieldDescriptor fieldDescriptor : messageDescriptor.fieldDescriptors) {
            target.append("\n    public static final byte[] ");
            appendFieldKeyConstantName(target, fieldDescriptor.fieldName);
            target.append(" = ");
            target.append("new byte[]{");
            for (byte byteVal : fieldDescriptor.fieldKeyValue) {
                target.append(String.valueOf(byteVal));
                target.append(",");
            }
            target.delete(target.length() - 1, target.length());
            target.append("};");
        }
    }

    private static void appendFieldKeyConstantName(StringBuilder target, String fieldName) {
        target.append(fieldName).append("Key");
    }

    private static void generateReadMethods(StringBuilder target, MessageDescriptor messageDescriptor) {
        target.append("\n    public void read(IonReader reader) {");
        for (FieldDescriptor fieldDescriptor : messageDescriptor.fieldDescriptors) {
            target.append("\n        ");
            appendFieldReadMethodName(target, fieldDescriptor);
            target.append("(reader);");
        }

        target.append("\n    }");

        for (FieldDescriptor fieldDescriptor : messageDescriptor.fieldDescriptors) {
            target.append("\n    protected void ");
            appendFieldReadMethodName(target, fieldDescriptor);

            target.append("(IonReader reader) {\n");
            target.append("        if(reader.fieldType == IonFieldTypes.KEY_SHORT) {");
            target.append("\n            if(reader.matches(");
            appendFieldKeyConstantName(target, fieldDescriptor.fieldName);
            target.append(")){");
            target.append("\n                reader.nextParse();");
            if (fieldDescriptor.isByteArrayType()) {
                target.append("\n                this.").append(fieldDescriptor.fieldName).append("Source = reader.source;");
                target.append("\n                this.").append(fieldDescriptor.fieldName).append("Offset = reader.index;");
                target.append("\n                this.").append(fieldDescriptor.fieldName).append("Length = reader.fieldLength;");
            } else if (fieldDescriptor.fieldType == IonFieldTypes.INT_POS) {
                target.append("\n                this.").append(fieldDescriptor.fieldName).append(" = reader.readInt64();");
            } else if (fieldDescriptor.fieldType == IonFieldTypes.BOOLEAN) {
                target.append("\n                this.").append(fieldDescriptor.fieldName).append(" = reader.readBoolean();");
            } else if (fieldDescriptor.fieldType == IonFieldTypes.FLOAT) {
                target.append("\n                this.").append(fieldDescriptor.fieldName).append(" = reader.readFloat64();");
            } else if (fieldDescriptor.fieldType == IonFieldTypes.UTC_DATE_TIME) {
                target.append("\n                this.").append(fieldDescriptor.fieldName).append(" = reader.readUtcCalendar();");
            }


            target.append("\n                reader.nextParse();");
            target.append("\n            }");
            target.append("\n        }");
            target.append("\n    }");
        }
    }

    private static void appendFieldReadMethodName(StringBuilder target, FieldDescriptor fieldDescriptor) {
        target.append("read").append(fieldDescriptor.getFieldNameFirstCharUppercase());
    }

    private static void generateWriteMethod(StringBuilder target, MessageDescriptor messageDescriptor) {
        target.append("\n    public void write(IonWriter writer) {");

        for (FieldDescriptor fieldDescriptor : messageDescriptor.fieldDescriptors) {
            target.append("\n        ");
            appendFieldWriteMethodName(target, fieldDescriptor.fieldName);
            target.append("(writer);");
        }
        target.append("\n    }");


        for (FieldDescriptor fieldDescriptor : messageDescriptor.fieldDescriptors) {
            target.append("\n    protected void ");
            appendFieldWriteMethodName(target, fieldDescriptor.fieldName);

            target.append("(IonWriter writer) {");
            if (fieldDescriptor.fieldKeyValue.length < 16) {
                target.append("\n        writer.writeKeyShort(");
            } else {
                target.append("\n        writer.writeKey(");
            }
            appendFieldKeyConstantName(target, fieldDescriptor.fieldName);
            target.append(");");

            if (fieldDescriptor.fieldType == IonFieldTypes.BYTES) {
                target.append("\n        writer.writeBytes(");
                target.append(fieldDescriptor.fieldName).append("Source, ");
                target.append(fieldDescriptor.fieldName).append("Offset, ");
                target.append(fieldDescriptor.fieldName).append("Length);");
            } else if (fieldDescriptor.fieldType == IonFieldTypes.BOOLEAN) {
                target.append("\n        writer.writeBytes(");
                target.append(fieldDescriptor.fieldName).append(");");
            } else if (fieldDescriptor.fieldType == IonFieldTypes.INT_POS) {
                target.append("\n        writer.writeInt64(");
                target.append(fieldDescriptor.fieldName).append(");");
            } else if (fieldDescriptor.fieldType == IonFieldTypes.FLOAT) {
                target.append("\n        writer.writeFloat64(");
                target.append(fieldDescriptor.fieldName).append(");");
            } else if (fieldDescriptor.fieldType == IonFieldTypes.UTF_8) {
                target.append("\n        writer.writeUtf8(");
                target.append(fieldDescriptor.fieldName).append("Source, ");
                target.append(fieldDescriptor.fieldName).append("Offset, ");
                target.append(fieldDescriptor.fieldName).append("Length);");
            } else if (fieldDescriptor.fieldType == IonFieldTypes.UTF_8_SHORT) {
                target.append("\n        writer.writeUtf8(");
                target.append(fieldDescriptor.fieldName).append("Source, ");
                target.append(fieldDescriptor.fieldName).append("Offset, ");
                target.append(fieldDescriptor.fieldName).append("Length);");
            } else if (fieldDescriptor.fieldType == IonFieldTypes.UTC_DATE_TIME) {
                target.append("\n        writer.writeUtc(");
                target.append(fieldDescriptor.fieldName).append(", 9);");
            }

            target.append("\n    }");
        }
    }

    private static void appendFieldWriteMethodName(StringBuilder target, String fieldName) {
        target.append("write").append(fieldName.substring(0, 1).toUpperCase())
                .append(fieldName.substring(1));
    }


    private static void generateFields(StringBuilder target, MessageDescriptor messageDescriptor) {
        for (FieldDescriptor fieldDescriptor : messageDescriptor.fieldDescriptors) {
            target.append("\n    public ");
            if (fieldDescriptor.isByteArrayType()) {
                target.append("byte[] ").append(fieldDescriptor.getFieldNameFirstCharLowercase()).append("Source;\n");
                target.append("    public int ").append(fieldDescriptor.fieldName).append("Offset;\n");
                target.append("    public int ").append(fieldDescriptor.fieldName).append("Length;\n");
            } else {
                target.append(fieldDescriptor.getFieldType());
                target.append(" ");
                target.append(fieldDescriptor.fieldName);
                target.append(";\n");
            }
        }
    }

    private static void generateSetters(StringBuilder target, MessageDescriptor messageDescriptor) {
        for (FieldDescriptor fieldDescriptor : messageDescriptor.fieldDescriptors) {
            if (fieldDescriptor.isByteArrayType()) {
                //generate a setter method which takes a byte array as parameter.
                target.append("\n    public void set");
                target.append(fieldDescriptor.getFieldNameFirstCharUppercase());
                target.append("(byte[] source){");
                target.append("\n        this.").append(fieldDescriptor.fieldName).append("Source = source;");
                target.append("\n        this.").append(fieldDescriptor.fieldName).append("Offset = 0;");
                target.append("\n        this.").append(fieldDescriptor.fieldName).append("Length = source.length;");
                target.append("\n    }");

                //generate a setter method which takes a byte array, offset and length as parameters.
                target.append("\n    public void set");
                target.append(fieldDescriptor.getFieldNameFirstCharUppercase());
                target.append("(byte[] source, int offset, int length){");
                target.append("\n        this.").append(fieldDescriptor.fieldName).append("Source = source;");
                target.append("\n        this.").append(fieldDescriptor.fieldName).append("Offset = offset;");
                target.append("\n        this.").append(fieldDescriptor.fieldName).append("Length = length;");
                target.append("\n    }");

                if(fieldDescriptor.getFieldType().equals("String")){
                    target.append("\n    public void set");
                    target.append(fieldDescriptor.getFieldNameFirstCharUppercase());
                    target.append("(String value){");
                    target.append("\n        try{");
                    target.append("\n            this.").append(fieldDescriptor.getFieldNameFirstCharLowercase())
                            .append("Source = value.getBytes(\"UTF-8\");");
                    target.append("\n            this.").append(fieldDescriptor.getFieldNameFirstCharLowercase())
                            .append("Offset = 0;");
                    target.append("\n            this.").append(fieldDescriptor.getFieldNameFirstCharLowercase())
                            .append("Length = this.").append(fieldDescriptor.getFieldNameFirstCharLowercase())
                            .append("Source.length;");
                    target.append("\n        } catch (UnsupportedEncodingException e) { /* will never happen - UTF-8 is supported */ }");
                    target.append("\n    }");
                }
            } else {
                target.append("\n    public void set");
                target.append(fieldDescriptor.getFieldNameFirstCharUppercase());
                target.append("(");
                target.append(fieldDescriptor.getFieldType());
                target.append(" value){");
                target.append("\n        this.").append(fieldDescriptor.getFieldNameFirstCharLowercase())
                        .append(" = value;");
                target.append("\n    }");
            }
        }
    }

    private static void generateGetters(StringBuilder target, MessageDescriptor messageDescriptor) {
        for (FieldDescriptor fieldDescriptor : messageDescriptor.fieldDescriptors) {
            if (fieldDescriptor.isByteArrayType() && !fieldDescriptor.getFieldType().equals("String")) {
                //no getter for these field types - just access the byte arrays, offset and length directly
            } else if(fieldDescriptor.getFieldType().equals("String")) {
                target.append("\n    public String get");
                target.append(fieldDescriptor.getFieldNameFirstCharUppercase());
                target.append("() {");
                target.append("\n        try{");
                target.append("\n            return new String(");
                target.append(fieldDescriptor.getFieldNameFirstCharLowercase()).append("Source");
                target.append(", ");
                target.append(fieldDescriptor.getFieldNameFirstCharLowercase()).append("Offset");
                target.append(", ");
                target.append(fieldDescriptor.getFieldNameFirstCharLowercase()).append("Length");
                target.append(", \"UTF-8\");");
                target.append("\n        } catch(UnsupportedEncodingException e){ return null; /* will never happen - UTF-8 is supported */ }");
                target.append("\n    }");
            } else {
                target.append("\n    public ");
                target.append(fieldDescriptor.getFieldType());
                target.append(" get");
                target.append(fieldDescriptor.getFieldNameFirstCharUppercase());
                target.append("(){");
                target.append("\n        return this.").append(fieldDescriptor.getFieldNameFirstCharLowercase()).append(";");

                target.append("\n    }");

            }
        }

    }

}
