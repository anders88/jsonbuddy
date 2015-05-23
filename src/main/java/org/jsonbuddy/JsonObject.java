package org.jsonbuddy;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonObject extends JsonNode {
    private final Map<String,JsonNode> values;

    public JsonObject(JsonObjectFactory jsonObjectFactory) {
        Map<String, JsonNode> nodeMap = jsonObjectFactory.values.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, en -> en.getValue().create()));
        values = Collections.unmodifiableMap(nodeMap);
    }

    public Optional<String> stringValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonSimpleValue)
                .map(n -> ((JsonSimpleValue) n).value());
    }
}
