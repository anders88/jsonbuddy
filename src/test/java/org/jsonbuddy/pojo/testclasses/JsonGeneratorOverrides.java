package org.jsonbuddy.pojo.testclasses;

import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.OverridesJsonGenerator;

public class JsonGeneratorOverrides implements OverridesJsonGenerator {
    @Override
    public Object jsonValue() {
        return new JsonObject().put("myOverriddenValue",42);
    }
}
