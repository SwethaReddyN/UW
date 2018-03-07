package keyvaluestore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenericNode {
    
    private static final int STORE_LENGTH = 65000;
    private static final Map<String, String> KEY_VALUE_STORE = new ConcurrentHashMap<String, String>();   
    
    public String put(String key, String value) {
        
        KEY_VALUE_STORE.put(key, value);
        return "put key = " + key;
    }
    
    public String get(String key) {
        
        String value = KEY_VALUE_STORE.get(key);
        if(value == null)
            return "Key Value pair not present";
        return "get key = " + key + ", val = " + value;
    }
    
    public String delete(String key) {
        
        String value = KEY_VALUE_STORE.remove(key);
        if(value == null)
            return "Key Value pair not present";
        return "Deleted key = " + key + ", value = " + value;
    }
    
    public String store() {
        
        String store = "";
        synchronized (KEY_VALUE_STORE) {
            for (Map.Entry<String, String> entry : KEY_VALUE_STORE.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                store += "key = " + key + ", val = " + value + System.lineSeparator();
            }
        }
        
        if(store.trim().length() == 0)
            return "Store empty";
        if(store.length() > STORE_LENGTH) {
            store = "TRIMMED:" + store;
            return store.substring(0, STORE_LENGTH);
        }
        return store;
    }
}