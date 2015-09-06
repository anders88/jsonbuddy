package org.jsonbuddy.pojo.testclasses;

import java.util.Map;

public class ClassWithMap {
    public final Map<String,String> properties;

    public ClassWithMap() {
        properties = null;
    }

    public ClassWithMap(Map<String, String> properties) {
        this.properties = properties;
    }
}
