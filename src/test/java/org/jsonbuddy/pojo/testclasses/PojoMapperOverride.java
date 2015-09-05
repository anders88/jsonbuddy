package org.jsonbuddy.pojo.testclasses;

import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.PojoClassMapper;

public class PojoMapperOverride implements PojoClassMapper<ClassWithAnnotation> {
    @Override
    public ClassWithAnnotation map(JsonObject jsonObject) {
        return new ClassWithAnnotation("overridden");
    }
}
