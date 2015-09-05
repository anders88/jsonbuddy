package org.jsonbuddy.pojo.testclasses;


import org.jsonbuddy.pojo.OverrideMapper;

@OverrideMapper(using = PojoMapperOverride.class)
public class ClassWithAnnotation {
    public final String value;

    public ClassWithAnnotation(String value) {
        this.value = value;
    }
}
