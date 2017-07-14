package com.nanosai.gridops.ion.codec;

import com.nanosai.gridops.codegen.FieldDescriptor;
import com.nanosai.gridops.ion.IonFieldTypes;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by jjenkov on 17/06/2017.
 */
public class FieldDescriptorTest {

    @Test
    public void testConstructors() {
       FieldDescriptor fieldDescriptor = new FieldDescriptor();
       assertNull(fieldDescriptor.fieldName);
       assertNull(fieldDescriptor.fieldKeyValue);
       assertEquals(-1, fieldDescriptor.fieldType);

       fieldDescriptor = new FieldDescriptor("email", IonFieldTypes.UTF_8, new byte[]{99});
       assertEquals("email", fieldDescriptor.fieldName);
       assertEquals(99, fieldDescriptor.fieldKeyValue[0]);
       assertEquals(IonFieldTypes.UTF_8, fieldDescriptor.fieldType);
    }
}
