package org.jsonbuddy.pojo.testclasses;

public class ClassWithStaticFieldsFromInterface implements InterfaceWithStaticFields {

    public static final String CONSTANT = "some constant";
    public static final String COMPUTED_FIELD = computeField();

    private static String computeField() {
        return "computed";
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
