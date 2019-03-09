package org.jsonbuddy;

import org.jsonbuddy.pojo.OverridesJsonGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class JsonFactory {
    public static JsonObject jsonObject() {
        return new JsonObject();
    }

    public static JsonArray jsonArray() {
        return new JsonArray();
    }

    public static JsonString jsonString(String text) {
        return new JsonString(text);
    }

    public static JsonNumber jsonNumber(Number number) {
        return new JsonNumber(number);
    }

    public static JsonBoolean jsonTrue() {
        return new JsonBoolean(true);
    }

    public static JsonBoolean jsonFalse() {
        return new JsonBoolean(false);
    }

    public static JsonBoolean jsonBoolean(boolean value) {
        return new JsonBoolean(value);
    }

    public static JsonString jsonInstant(Instant instant) {
        return new JsonString(instant.toString());
    }

    public static JsonNode jsonNode(Object o) {
        if (o instanceof JsonNode) {
            return ((JsonNode)o);
        } else if (o instanceof OverridesJsonGenerator) {
            return ((OverridesJsonGenerator) o).jsonValue();
        } else if (o instanceof CharSequence) {
            return new JsonString(o.toString());
        } else if (o instanceof Instant) {
            return jsonInstant((Instant)o);
        } else if (o instanceof Boolean) {
            return new JsonBoolean((Boolean)o);
        } else if (o instanceof Integer) {
            return new JsonNumber(((Integer)o).longValue());
        } else if (o instanceof Number) {
            return new JsonNumber((Number)o);
        } else if (o instanceof List) {
            return new JsonArray().addAll((List<String>)o);
        } else if (o instanceof Enum || o instanceof  UUID) {
            return new JsonString(o.toString());
        } else if (o == null) {
            return new JsonNull();
        } else {
            throw new IllegalArgumentException("Invalid JsonNode class " + o);
        }
    }
}
