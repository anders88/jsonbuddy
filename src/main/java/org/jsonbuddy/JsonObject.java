package org.jsonbuddy;

import org.jsonbuddy.pojo.JsonNumber;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JsonObject extends JsonNode {
    private final Map<String,JsonNode> values;


    public JsonObject() {
        this.values = new HashMap<>();
    }

    private JsonObject(Map<String,JsonNode> values) {
        this.values = values;
    }

    public Optional<String> stringValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonValue)
                .map(n -> ((JsonValue) n).stringValue());
    }

    public Optional<Long> longValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(JsonObject::isLong)
                .map(JsonObject::mapToLong);
    }

    private static boolean isLong(JsonNode jsonNode) {
        if (jsonNode instanceof JsonNumber) {
            return true;
        }
        if (jsonNode instanceof JsonString) {
            try {
                Long.parseLong(((JsonString) jsonNode).stringValue());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private static long mapToLong(JsonNode jsonNode) {
        if (jsonNode instanceof JsonNumber) {
            return ((JsonNumber) jsonNode).longValue();
        }
        return Long.parseLong(jsonNode.textValue());
    }

    public Optional<Boolean> booleanValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonBoolean)
                .map(n -> ((JsonBoolean) n).boolValue());
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
        if (value(key).isPresent() && value(key).get().equals(new JsonNull())) {
            return null;
        }
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

    public Instant requiredInstant(String key) {
        JsonValue val = value(key)
                .filter(no -> (no instanceof JsonString))
                        .map(no -> (JsonValue) no)
                        .orElseThrow(throwKeyNotPresent(key));
        String text = val.textValue();
        return Instant.parse(text);
    }

    public Optional<Instant> instantValue(String key) {
        Optional<JsonValue> val = value(key)
                .filter(no -> (no instanceof JsonString))
                .map(no -> (JsonValue) no);
        if (!val.isPresent()) {
            return Optional.empty();
        }
        String text = val.get().textValue();
        return Optional.of(Instant.parse(text));
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

    public JsonObject put(String key, JsonNode jsonNode) {
        values.put(key, jsonNode);
        return this;
    }

    public JsonObject put(String key,String value) {
        return put(key, JsonFactory.jsonText(value));
    }

    public JsonObject put(String key,double value) {
        return put(key, JsonFactory.jsonNumber(value));
    }

    public JsonObject put(String key,long value) {
        return put(key, JsonFactory.jsonNumber(value));
    }

    public JsonObject put(String key,boolean value) {
        return put(key, JsonFactory.jsonBoolean(value));
    }

    public JsonObject put(String key,Enum<?> value) {
        return put(key, Optional.of(value).map(Object::toString).orElse(null));
    }

    public Set<String> keys() {
        return values.keySet();
    }

    public JsonObject put(String key, List<String> values) {
        return put(key, JsonFactory.jsonArray().add(values));
    }

    public Optional<JsonNode> removeValue(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(values.remove(key));
    }

    public JsonObject put(String key, Instant instant) {
        return put(key, JsonFactory.jsonInstance(instant));
    }

    @Override
    public JsonObject deepClone() {
        Map<String, JsonNode> cloned = values.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().deepClone()));
        Map<String, JsonNode> newValues = new HashMap<>();
        newValues.putAll(cloned);
        return new JsonObject(newValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonObject)) return false;
        JsonObject that = (JsonObject) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }


}
