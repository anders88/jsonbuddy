package org.jsonbuddy;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
        return get(key, JsonValue.class).map(JsonValue::stringValue);
    }

    @Override
    public String requiredString(String key) throws JsonValueNotPresentException {
        return stringValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    public Optional<Double> doubleValue(String key) {
        return numberValue(key).map(JsonNumber::doubleValue);
    }

    public double requiredDouble(String key) throws JsonValueNotPresentException {
        return doubleValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    public Optional<Long> longValue(String key) {
        return numberValue(key).map(JsonNumber::longValue);
    }

    public long requiredLong(String key) throws JsonValueNotPresentException{
        return longValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    public Optional<JsonNumber> numberValue(String key) {
        JsonNode node = values.get(key);
        if (node instanceof JsonValue) {
            try {
                return Optional.of(new JsonNumber(((JsonValue)node)));
            } catch (NumberFormatException e) {
                throw new JsonValueNotPresentException(key + " is not numeric");
            }
        } else if (node == null) {
            return Optional.empty();
        } else {
            throw new JsonValueNotPresentException(key + " is not numeric");
        }
    }

    public Optional<Boolean> booleanValue(String key) {
        return get(key, JsonBoolean.class).map(JsonBoolean::boolValue);
    }

    public boolean requiredBoolean(String key) throws JsonValueNotPresentException{
        return booleanValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    public Optional<JsonObject> objectValue(String key) {
        return get(key, JsonObject.class);
    }

    public JsonObject requiredObject(String key) throws JsonValueNotPresentException{
        return objectValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    public Optional<Instant> instantValue(String key) {
        Optional<JsonValue> val = value(key)
                .filter(no -> (no instanceof JsonString))
                .map(no -> (JsonValue) no);
        if (!val.isPresent()) {
            return Optional.empty();
        }
        String text = val.get().stringValue();
        return Optional.of(Instant.parse(text));
    }

    public Instant requiredInstant(String key) {
        return instantValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    public Optional<JsonArray> arrayValue(String key) {
        return get(key, JsonArray.class);
    }

    public Optional<JsonNode> value(String key) {
        return Optional.ofNullable(values.get(key));
    }

    public <T extends JsonNode> Optional<T> get(String key, Class<T> t) {
        return value(key)
                .filter(node -> t.isAssignableFrom(node.getClass()))
                .map(node -> (T) node);
    }

    private Supplier<JsonValueNotPresentException> throwKeyNotPresent(String key) {
        return () -> new JsonValueNotPresentException(String.format("Required key '%s' does not exist",key));
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

    public JsonObject put(String key, Instant instant) {
        return put(key, JsonFactory.jsonInstance(instant));
    }

    public JsonObject put(String key, List<String> values) {
        return put(key, JsonFactory.jsonArray().addAll(values));
    }

    public Set<String> keys() {
        return values.keySet();
    }

    public Optional<JsonNode> remove(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(values.remove(key));
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
