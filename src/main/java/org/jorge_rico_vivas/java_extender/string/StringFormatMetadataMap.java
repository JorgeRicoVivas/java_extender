package org.jorge_rico_vivas.java_extender.string;

import java.util.HashMap;

public class StringFormatMetadataMap implements StringFormatMetadata {
    final HashMap<String, String> metadata;

    public StringFormatMetadataMap(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    public StringFormatMetadataMap() {
        this.metadata = new HashMap<>();
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String get(String key) {
        return metadata.get(key);
    }

    @Override
    public void set(String key, String value) {
        metadata.put(key, value);
    }
}
