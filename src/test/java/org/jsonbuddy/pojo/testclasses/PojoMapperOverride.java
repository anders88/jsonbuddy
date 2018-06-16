package org.jsonbuddy.pojo.testclasses;

import org.jsonbuddy.JsonNode;
import org.jsonbuddy.pojo.JsonPojoBuilder;

public class PojoMapperOverride implements JsonPojoBuilder<ClassWithAnnotation> {
    public static boolean returnNull = false;
    @Override
    public ClassWithAnnotation build(JsonNode jsonNode) {
        if (returnNull) {
            return null;
        }
        return new ClassWithAnnotation("overridden");
    }
}
