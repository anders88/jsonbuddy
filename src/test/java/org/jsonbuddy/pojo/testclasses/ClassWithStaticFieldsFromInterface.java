package org.jsonbuddy.pojo.testclasses;

public class ClassWithStaticFieldsFromInterface implements InterfaceWithStaticFields {

    public static final String CONSTANT = "some constant";

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
