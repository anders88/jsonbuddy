package org.jsonbuddy.pojo.testclasses;

import org.jsonbuddy.JsonNode;
import org.jsonbuddy.pojo.JsonPojoBuilder;

public class PojoMapperOverride implements JsonPojoBuilder<ClassWithAnnotation> {
    @Override
    public ClassWithAnnotation build(JsonNode jsonNode) {
        return new ClassWithAnnotation("overridden");
    }
}
