package com.nanosai.gridops.iap.directory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jjenkov on 10-09-2016.
 */
public class Directory {

    private Map<DirectoryKey, Object> entries = new HashMap<>();

    public Object lookup(DirectoryKey key) {
        return this.entries.get(key);
    }

    public void register(DirectoryKey key, Object value){
        this.entries.put(key, value);
    }

}
