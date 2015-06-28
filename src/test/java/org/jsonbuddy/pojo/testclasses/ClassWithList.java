package org.jsonbuddy.pojo.testclasses;

import java.util.List;

public class ClassWithList {
    public final String name;
    public final List<String> children;

    public ClassWithList() {
        name = null;
        children = null;
    }

    public ClassWithList(String name, List<String> children) {
        this.name = name;
        this.children = children;
    }
}
