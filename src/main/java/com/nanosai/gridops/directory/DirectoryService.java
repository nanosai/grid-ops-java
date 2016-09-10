package com.nanosai.gridops.directory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jjenkov on 10-09-2016.
 */
public class DirectoryService {

    private Map<DirectoryKey, DirectoryValue> entries = new HashMap<>();

    public DirectoryValue lookup(DirectoryKey key) {
        return this.entries.get(key);
    }


}
