package org.jsonbuddy.pojo.testclasses;

import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.JsonPojoBuilder;
import org.jsonbuddy.pojo.OverrideMapper;

import java.util.Optional;

@OverrideMapper(using = ClassWithPojoOverride.MapperOverride.class)
public class ClassWithPojoOverride {
    public ClassWithPojoOverride(String value) {
        this.value = value;
    }

    public final String value;


    public static class MapperOverride implements JsonPojoBuilder<ClassWithPojoOverride> {
        @Override
        public ClassWithPojoOverride build(JsonNode jsonNode) {
            JsonObject object = (JsonObject) jsonNode;
            Optional<String> value = object.stringValue("value");
            return new ClassWithPojoOverride("overridden " + value.orElse("empty"));
        }
    }


}
