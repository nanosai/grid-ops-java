package com.nanosai.gridops.ion.write;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.ion.IonFieldTypes;
import com.nanosai.gridops.ion.pojos.RecursiveArrayPojo;
import com.nanosai.gridops.ion.pojos.RecursivePojo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Created by jjenkov on 18/03/2017.
 */
public class IonObjectWriterRecursivePojoTests {


    @Test
    public void testWriteRecursivePojo() {
        IonObjectWriter ionObjectWriter = GridOps.ionObjectWriter(RecursivePojo.class);

        IonFieldWriterObject ionFieldWriterObject = (IonFieldWriterObject) ionObjectWriter.fieldWriters[1];

        assertEquals(IonFieldWriterString.class, ionObjectWriter.fieldWriters[0].getClass());
        assertEquals(IonFieldWriterObject.class, ionObjectWriter.fieldWriters[1].getClass());
        assertSame(ionObjectWriter.fieldWriters[1], ionFieldWriterObject.fieldWriters[1]);

        RecursivePojo root   = new RecursivePojo();
        RecursivePojo child1 = new RecursivePojo();
        RecursivePojo child2 = new RecursivePojo();

        root.setName("root");
        child1.setName("child1");
        child2.setName("child2");

        root.setChild1(child1);
        root.setChild2(child2);

        byte[] dest = new byte[1024];

        ionObjectWriter.writeObject(root, 2, dest, 0);

        int index = 0;
        assertEquals(IonFieldTypes.OBJECT << 4 | 2, 255 & dest[index++]);
        assertEquals(0, 255 & dest[index++]);
        assertEquals(86, 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 4, 255 & dest[index++]);
        assertEquals('n', 255 & dest[index++]);
        assertEquals('a', 255 & dest[index++]);
        assertEquals('m', 255 & dest[index++]);
        assertEquals('e', 255 & dest[index++]);

        assertEquals(IonFieldTypes.UTF_8_SHORT << 4 | 4, 255 & dest[index++]);
        assertEquals('r', 255 & dest[index++]);
        assertEquals('o', 255 & dest[index++]);
        assertEquals('o', 255 & dest[index++]);
        assertEquals('t', 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 6, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('1', 255 & dest[index++]);


        assertEquals(IonFieldTypes.OBJECT << 4 | 2, 255 & dest[index++]);
        assertEquals(0, 255 & dest[index++]);
        assertEquals(28, 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 4, 255 & dest[index++]);
        assertEquals('n', 255 & dest[index++]);
        assertEquals('a', 255 & dest[index++]);
        assertEquals('m', 255 & dest[index++]);
        assertEquals('e', 255 & dest[index++]);

        assertEquals(IonFieldTypes.UTF_8_SHORT << 4 | 6, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('1', 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 6, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('1', 255 & dest[index++]);

        assertEquals(IonFieldTypes.OBJECT << 4 | 0, 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 6, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('2', 255 & dest[index++]);

        assertEquals(IonFieldTypes.OBJECT << 4 | 0, 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 6, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('2', 255 & dest[index++]);

        assertEquals(IonFieldTypes.OBJECT << 4 | 2, 255 & dest[index++]);
        assertEquals(0, 255 & dest[index++]);
        assertEquals(28, 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 4, 255 & dest[index++]);
        assertEquals('n', 255 & dest[index++]);
        assertEquals('a', 255 & dest[index++]);
        assertEquals('m', 255 & dest[index++]);
        assertEquals('e', 255 & dest[index++]);

        assertEquals(IonFieldTypes.UTF_8_SHORT << 4 | 6, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('2', 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 6, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('1', 255 & dest[index++]);

        assertEquals(IonFieldTypes.OBJECT << 4 | 0, 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 6, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('2', 255 & dest[index++]);

        assertEquals(IonFieldTypes.OBJECT << 4 | 0, 255 & dest[index++]);

    }


    @Test
    public void testWriteRecursiveArrayPojo() {
        IonObjectWriter ionObjectWriter = GridOps.ionObjectWriter(RecursiveArrayPojo.class);

        IonFieldWriterTable ionFieldWriterTable = (IonFieldWriterTable) ionObjectWriter.fieldWriters[1];

        assertEquals(IonFieldWriterString.class, ionObjectWriter.fieldWriters[0].getClass());
        assertEquals(IonFieldWriterTable.class, ionObjectWriter.fieldWriters[1].getClass());
        assertSame(ionObjectWriter.fieldWriters[1], ionFieldWriterTable.fieldWritersForArrayType[1]);

        RecursiveArrayPojo root   = new RecursiveArrayPojo();
        RecursiveArrayPojo child1 = new RecursiveArrayPojo();
        RecursiveArrayPojo child2 = new RecursiveArrayPojo();

        root.setChildren(new RecursiveArrayPojo[2]);
        root.getChildren()[0] = new RecursiveArrayPojo();
        root.getChildren()[1] = new RecursiveArrayPojo();

        root.setName("root");
        root.getChildren()[0].setName("child1");
        root.getChildren()[1].setName("child2");

        byte[] dest = new byte[1024];

        ionObjectWriter.writeObject(root, 2, dest, 0);

        int index = 0;
        assertEquals(IonFieldTypes.OBJECT << 4 | 2, 255 & dest[index++]);
        assertEquals(0, 255 & dest[index++]);
        assertEquals(52, 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 4, 255 & dest[index++]);
        assertEquals('n', 255 & dest[index++]);
        assertEquals('a', 255 & dest[index++]);
        assertEquals('m', 255 & dest[index++]);
        assertEquals('e', 255 & dest[index++]);

        assertEquals(IonFieldTypes.UTF_8_SHORT << 4 | 4, 255 & dest[index++]);
        assertEquals('r', 255 & dest[index++]);
        assertEquals('o', 255 & dest[index++]);
        assertEquals('o', 255 & dest[index++]);
        assertEquals('t', 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 8, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('r', 255 & dest[index++]);
        assertEquals('e', 255 & dest[index++]);
        assertEquals('n', 255 & dest[index++]);

        assertEquals(IonFieldTypes.TABLE << 4 | 2, 255 & dest[index++]);
        assertEquals( 0, 255 & dest[index++]);
        assertEquals(30, 255 & dest[index++]);
        //assertEquals(IonFieldTypes.TABLE << 4 | 2, 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 4, 255 & dest[index++]);
        assertEquals('n', 255 & dest[index++]);
        assertEquals('a', 255 & dest[index++]);
        assertEquals('m', 255 & dest[index++]);
        assertEquals('e', 255 & dest[index++]);

        assertEquals(IonFieldTypes.KEY_SHORT << 4 | 8, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('r', 255 & dest[index++]);
        assertEquals('e', 255 & dest[index++]);
        assertEquals('n', 255 & dest[index++]);

        assertEquals(IonFieldTypes.UTF_8_SHORT << 4 | 6, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('1', 255 & dest[index++]);

        assertEquals(IonFieldTypes.TABLE << 4 | 0, 255 & dest[index++]);

        assertEquals(IonFieldTypes.UTF_8_SHORT << 4 | 6, 255 & dest[index++]);
        assertEquals('c', 255 & dest[index++]);
        assertEquals('h', 255 & dest[index++]);
        assertEquals('i', 255 & dest[index++]);
        assertEquals('l', 255 & dest[index++]);
        assertEquals('d', 255 & dest[index++]);
        assertEquals('2', 255 & dest[index++]);

        assertEquals(IonFieldTypes.TABLE << 4 | 0, 255 & dest[index++]);





    }


}
