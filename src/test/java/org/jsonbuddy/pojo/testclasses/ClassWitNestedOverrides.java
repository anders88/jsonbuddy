package org.jsonbuddy.pojo.testclasses;

public class ClassWitNestedOverrides {
    private final JsonGeneratorOverrides myNested;
    private final String myOvnValue;

    public ClassWitNestedOverrides(JsonGeneratorOverrides myNested, String myOvnValue) {
        this.myNested = myNested;
        this.myOvnValue = myOvnValue;
    }

    public JsonGeneratorOverrides getMyNested() {
        return myNested;
    }

    public String getMyOvnValue() {
        return myOvnValue;
    }
}
