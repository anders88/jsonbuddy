package org.jsonbuddy.pojo.testclasses;

import java.util.Map;

public class ClassWithEmbeddedGetSetMap {
    private Map<String,SimpleWithName> namesx = null;

    public Map<String, SimpleWithName> getNames() {
        return namesx;
    }

    public void setNames(Map<String, SimpleWithName> names) {
        this.namesx = names;
    }
}
