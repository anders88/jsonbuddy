package org.jsonbuddy.pojo.testclasses;

public class ClassWithGetterInterface {
    private final InterfaceWithMethod myInterface;

    public ClassWithGetterInterface(InterfaceWithMethod myInterface) {
        this.myInterface = myInterface;
    }

    public InterfaceWithMethod getMyInterface() {
        return myInterface;
    }
}
