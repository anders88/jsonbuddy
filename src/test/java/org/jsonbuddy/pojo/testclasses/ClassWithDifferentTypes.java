package org.jsonbuddy.pojo.testclasses;

public class ClassWithDifferentTypes {
    public final String text;
    public final Integer number;
    public final Boolean bool;
    public final Boolean falseBool;

    public ClassWithDifferentTypes() {
        text = null;
        number = 0;
        bool = false;
        falseBool = true;
    }

    public ClassWithDifferentTypes(String text, Integer number, Boolean bool, Boolean falseBool) {
        this.text = text;
        this.number = number;
        this.bool = bool;
        this.falseBool = falseBool;
    }

    public String a() {
        return "a";
    }

    public String getter_method() {
        return "";
    }

    public void getWithIllegalSignature() {
        // Dummy method to test getter signature detection
    }

    public String getWithParam(String s) {
        return s;
    }
}
