package org.jsonbuddy.pojo.testclasses;

public class ClassWithDifferentTypes {
    public final String text;
    public final Integer number;
    public final Boolean bool;

    public ClassWithDifferentTypes() {
        text = null;
        number = 0;
        bool = false;
    }

    public ClassWithDifferentTypes(String text, Integer number, Boolean bool) {
        this.text = text;
        this.number = number;
        this.bool = bool;
    }
}
