package org.jsonbuddy.pojo.testclasses;

import java.util.List;
import java.util.Map;

public class ClassWithInterfaceListAndMapMethods {
    private List<InterfaceWithMethod> myList;
    private Map<String,InterfaceWithMethod> myMap;

    public ClassWithInterfaceListAndMapMethods() {
    }

    public List<InterfaceWithMethod> getMyList() {
        return myList;
    }

    public void setMyList(List<InterfaceWithMethod> myList) {
        this.myList = myList;
    }

    public Map<String, InterfaceWithMethod> getMyMap() {
        return myMap;
    }

    public void setMyMap(Map<String, InterfaceWithMethod> myMap) {
        this.myMap = myMap;
    }
}
