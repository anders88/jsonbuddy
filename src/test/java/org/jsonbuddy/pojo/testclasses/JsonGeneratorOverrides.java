package org.jsonbuddy.pojo.testclasses;

import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.pojo.OverridesJsonGenerator;

public class JsonGeneratorOverrides implements OverridesJsonGenerator {
    @Override
    public JsonNode jsonValue() {
        return JsonFactory.jsonObject().withValue("myOverriddenValue",42);
    }
}
