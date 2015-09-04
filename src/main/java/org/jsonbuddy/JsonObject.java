package org.jsonbuddy;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JsonObject extends JsonNode {
    private final Map<String,JsonNode> values = new HashMap<>();


    public JsonObject() {

    }

    public Optional<String> stringValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonSimpleValue)
                .map(n -> ((JsonSimpleValue) n).stringValue());
    }

    public Optional<Long> longValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonLong)
                .map(n -> ((JsonLong) n).longValue());
    }

    public Optional<Boolean> booleanValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonLong)
                .map(n -> ((JsonBooleanValue) n).boolValue());
    }

    public Optional<JsonObject> objectValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonObject)
                .map(n -> (JsonObject) n);
    }

    public Optional<JsonArray> arrayValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonArray)
                .map(n -> (JsonArray) n);

    }

    public Optional<JsonNode> value(String key) {
        return Optional.ofNullable(values.get(key));
    }

    @Override
    public String requiredString(String key) throws JsonValueNotPresentException {
        return stringValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    private Supplier<JsonValueNotPresentException> throwKeyNotPresent(String key) {
        return () -> new JsonValueNotPresentException(String.format("Required key '%s' does not exsist",key));
    }


    public long requiredLong(String key) throws JsonValueNotPresentException{
        return longValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    public boolean requiredBoolean(String key) throws JsonValueNotPresentException{
        return booleanValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    public JsonObject requiredObject(String key) throws JsonValueNotPresentException{
        return objectValue(key).orElseThrow(throwKeyNotPresent(key));
    }


    public JsonArray requiredArray(String key) {
        return arrayValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append("{");
        boolean notFirst = false;
        for (Map.Entry<String,JsonNode> entry : values.entrySet()) {
            if (notFirst) {
                printWriter.append(",");
            }
            notFirst = true;
            printWriter.append('"');
            printWriter.append(entry.getKey());
            printWriter.append("\":");
            entry.getValue().toJson(printWriter);
        }

        printWriter.append("}");
    }

    public JsonObject withValue(String key, JsonNode jsonNode) {
        values.put(key, jsonNode);
        return this;
    }

    public JsonObject withValue(String key,String value) {
        return withValue(key,JsonFactory.jsonText(value));
    }

    public JsonObject withValue(String key,long value) {
        return withValue(key, JsonFactory.jsonLong(value));
    }

    public JsonObject withValue(String key,boolean value) {
        return withValue(key, JsonFactory.jsonBoolean(value));
    }

    public JsonObject withValue(String key,Enum<?> value) {
        return withValue(key,Optional.of(value).map(Object::toString).orElse(null));
    }

    public Set<String> keys() {
        return values.keySet();
    }

    public JsonObject withValue(String key, List<String> values) {
        return withValue(key, JsonFactory.jsonArray().add(values));
    }

    public Optional<JsonNode> removeValue(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(values.remove(key));
    }

    public JsonObject withValue(String key, Instant instant) {
        return withValue(key,JsonFactory.jsonInstance(instant));
    }
}
