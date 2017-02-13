package com.nanosai.gridops.iap.directory;

import com.nanosai.gridops.id.Id;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jjenkov on 10-09-2016.
 */
public class Directory {

    private Map<Id, Object> entries = new HashMap<>();

    public Object lookup(Id key) {
        return this.entries.get(key);
    }

    public void register(Id key, Object value){
        this.entries.put(key, value);
    }

}
