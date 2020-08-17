package com.dveamer.batch.helper;

import java.util.HashMap;
import java.util.Map;

public class JobAsyncScopeUtil {

    private static final ThreadLocal<Map<String,Object>> storage = new ThreadLocal<>();

    public static void init() {
        storage.remove();
        storage.set(new HashMap<>());
    }

    public static Object get(String key) {
        Map<String,Object> map = storage.get();
        if(map==null) {
            throw new IllegalThreadStateException("Initialization is required.");
        }
        return map.get(key);
    }

    public static void set(String key, Object value) {
        Map<String,Object> map = storage.get();
        if(map==null) {
            throw new IllegalThreadStateException("Initialization is required.");
        }
        map.put(key, value);
    }

    public static void clear() {
        storage.remove();
    }

}
