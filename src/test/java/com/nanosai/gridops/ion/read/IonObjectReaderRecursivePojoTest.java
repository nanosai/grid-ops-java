package com.nanosai.gridops.ion.read;

import com.nanosai.gridops.GridOps;
import com.nanosai.gridops.ion.pojos.RecursiveArrayPojo;
import com.nanosai.gridops.ion.pojos.RecursivePojo;
import com.nanosai.gridops.ion.write.IonObjectWriter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by jjenkov on 25/03/2017.
 */
public class IonObjectReaderRecursivePojoTest {

    @Test
    public void testRecursiveObjectReading() {
        byte[] data = new byte[1024];
        IonObjectWriter ionObjectWriter = GridOps.ionObjectWriter(RecursivePojo.class);
        IonObjectReader ionObjectReader = GridOps.ionObjectReader(RecursivePojo.class);

        RecursivePojo pojoSource = new RecursivePojo();
        pojoSource.setName("root");

        RecursivePojo pojoSourceChild1 = new RecursivePojo();
        pojoSourceChild1.setName("child1");

        RecursivePojo pojoSourceChild2 = new RecursivePojo();
        pojoSourceChild2.setName("child2");

        pojoSource.setChild1(pojoSourceChild1);
        pojoSource.setChild2(pojoSourceChild2);

        ionObjectWriter.writeObject(pojoSource, 2, data, 0);

        RecursivePojo pojoDest = (RecursivePojo) ionObjectReader.read(data, 0);
        assertNotNull(pojoDest);
        assertEquals("root", pojoDest.getName());

        assertNotNull(pojoDest.getChild1());
        assertEquals("child1", pojoDest.getChild1().getName());
        assertNull(pojoDest.getChild1().getChild1());
        assertNull(pojoDest.getChild1().getChild2());

        assertNotNull(pojoDest.getChild2());
        assertEquals("child2", pojoDest.getChild2().getName());
        assertNull(pojoDest.getChild2().getChild1());
        assertNull(pojoDest.getChild2().getChild2());
    }


    @Test
    public void testRecursiveObjectTableReading() {
        byte[] data = new byte[1024];
        IonObjectWriter ionObjectWriter = GridOps.ionObjectWriter(RecursiveArrayPojo.class);
        IonObjectReader ionObjectReader = GridOps.ionObjectReader(RecursiveArrayPojo.class);

        RecursiveArrayPojo pojoSource = new RecursiveArrayPojo();
        pojoSource.setName("root");

        RecursiveArrayPojo child1 = new RecursiveArrayPojo();
        child1.setName("child1");

        RecursiveArrayPojo child2 = new RecursiveArrayPojo();
        child2.setName("child2");

        pojoSource.setChildren(new RecursiveArrayPojo[]{child1, child2});

        ionObjectWriter.writeObject(pojoSource, 2, data, 0);


        RecursiveArrayPojo pojoDest = (RecursiveArrayPojo) ionObjectReader.read(data, 0);

        assertNotNull(pojoDest);
        assertEquals("root", pojoDest.getName());

        assertNotNull(pojoDest.getChildren());
        assertEquals(2, pojoDest.getChildren().length);

        assertNotNull(pojoDest.getChildren()[0]);
        assertEquals("child1", pojoDest.getChildren()[0].getName());
        assertNull(pojoDest.getChildren()[0].getChildren());

        assertNotNull(pojoDest.getChildren()[1]);
        assertEquals("child2", pojoDest.getChildren()[1].getName());
        assertNull(pojoDest.getChildren()[1].getChildren());


    }
}
