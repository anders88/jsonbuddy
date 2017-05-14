package org.jsonbuddy.pojo.testclasses;

public class ClassWithGetterInterface {
    private InterfaceWithMethod myInterface;

    public ClassWithGetterInterface() {
    }

    public ClassWithGetterInterface(InterfaceWithMethod myInterface) {
        this.myInterface = myInterface;
    }

    public InterfaceWithMethod getMyInterface() {
        return myInterface;
    }
}
