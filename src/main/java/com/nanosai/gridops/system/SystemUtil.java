package com.nanosai.gridops.system;

/**
 * Various utilities which could be located somewhere else, but are located here for now.
 */
public class SystemUtil {


    static boolean equals(byte[] data1, int offset1, int length1, byte[] data2, int offset2, int length2) {
        if(length1 != length2){
            return false;
        }

        for(int i=0; i<length1; i++) {
            if(data1[offset1 + i ] != data2[offset2 + i]){
                return false;
            }
        }

        return true;
    }
}
