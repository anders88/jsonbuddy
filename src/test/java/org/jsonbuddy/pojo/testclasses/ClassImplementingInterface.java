package org.jsonbuddy.pojo.testclasses;

public class ClassImplementingInterface implements InterfaceWithMethod {
    private final String publicvalue;
    private final String privatevalue;

    public ClassImplementingInterface(String publicvalue, String privatevalue) {
        this.publicvalue = publicvalue;
        this.privatevalue = privatevalue;
    }

    public String getPublicvalue() {
        return publicvalue;
    }

    public String getPrivatevalue() {
        return privatevalue;
    }
}
