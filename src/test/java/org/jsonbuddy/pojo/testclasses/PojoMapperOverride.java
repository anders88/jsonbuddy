package org.jsonbuddy.pojo.testclasses;

import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.PojoClassMapper;

public class PojoMapperOverride implements PojoClassMapper<ClassWithAnnotation> {
    @Override
    public ClassWithAnnotation map(JsonNode jsonNode) {
        return new ClassWithAnnotation("overridden");
    }
}
