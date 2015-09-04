package org.jsonbuddy.pojo.testclasses;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

public class ClassWithJsonElements {
    public final String name;
    public final JsonObject myObject;
    public final JsonArray myArray;

    public ClassWithJsonElements() {
        name = null;
        myObject = null;
        myArray = null;
    }

    public ClassWithJsonElements(String name, JsonObject myObject, JsonArray myArray) {
        this.name = name;
        this.myObject = myObject;
        this.myArray = myArray;
    }
}
