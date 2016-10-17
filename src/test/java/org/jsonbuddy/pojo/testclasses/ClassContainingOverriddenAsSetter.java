package org.jsonbuddy.pojo.testclasses;

import java.util.List;

public class ClassContainingOverriddenAsSetter {
    private List<ClassWithPojoOverride> annonlist;

    public List<ClassWithPojoOverride> getAnnonlist() {
        return annonlist;
    }

    public void setAnnonlist(List<ClassWithPojoOverride> annonlist) {
        this.annonlist = annonlist;
    }
}
