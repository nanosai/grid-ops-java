package com.nanosai.gridops.tcp;

import com.nanosai.gridops.ion.IonFieldTypes;

/**
 * Created by jjenkov on 10-09-2016.
 */
public class IapUtil {

    public static int createMessage(byte[] dest, int offset){
        dest[offset + 0] = (byte) (255 & (IonFieldTypes.OBJECT << 4 | 2));
        dest[offset + 1] = 0;
        dest[offset + 2] = 12;

        dest[offset + 3] = (byte) (255 & (IonFieldTypes.KEY_SHORT << 4 | 2));
        dest[offset + 4] = 'k';
        dest[offset + 5] = '1';

        dest[offset + 6] = (byte) (255 & (IonFieldTypes.UTF_8_SHORT << 4 | 2));
        dest[offset + 7] = 'v';
        dest[offset + 8] = '1';

        dest[offset + 9] = (byte) (255 & (IonFieldTypes.KEY_SHORT << 4 | 2));
        dest[offset + 10] = 'k';
        dest[offset + 11] = '2';

        dest[offset + 12] = (byte) (255 & (IonFieldTypes.UTF_8_SHORT << 4 | 2));
        dest[offset + 13] = 'v';
        dest[offset + 14] = '2';

        return 15; //total message sourceLength in bytes.
    }
}
