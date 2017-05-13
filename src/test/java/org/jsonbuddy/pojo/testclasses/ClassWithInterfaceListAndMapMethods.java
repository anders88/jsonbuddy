package org.jsonbuddy.pojo.testclasses;

import java.util.List;
import java.util.Map;

public class ClassWithInterfaceListAndMapMethods {
    private List<InterfaceWithMethod> myList;
    private Map<String,InterfaceWithMethod> myMap;

    public List<InterfaceWithMethod> getMyList() {
        return myList;
    }

    public ClassWithInterfaceListAndMapMethods setMyList(List<InterfaceWithMethod> myList) {
        this.myList = myList;
        return this;
    }

    public Map<String, InterfaceWithMethod> getMyMap() {
        return myMap;
    }

    public ClassWithInterfaceListAndMapMethods setMyMap(Map<String, InterfaceWithMethod> myMap) {
        this.myMap = myMap;
        return this;
    }
}
