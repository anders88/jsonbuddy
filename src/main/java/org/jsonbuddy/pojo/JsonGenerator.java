package org.jsonbuddy.pojo;

import org.jsonbuddy.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonGenerator {
    public static JsonNode generate(Object object) {
        return new JsonGenerator().generateNode(object);
    }

    private JsonNode generateNode(Object object) {
        if (object == null) {
            return new JsonNullValue();
        }
        if (object instanceof String) {
            return JsonFactory.jsonText((String) object);
        }
        if ((object instanceof Integer))  {
            int i = (int) object;
            long l = i;
            return JsonFactory.jsonLong(l);
        }
        if (object instanceof Long) {
            return JsonFactory.jsonLong((Long) object);
        }
        if (object instanceof Double) {
            return JsonFactory.jsonDouble((Double) object);
        }
        if (object instanceof Float) {
            float f = (float) object;
            double d = f;
            return JsonFactory.jsonDouble(d);
        }
        if (object instanceof List) {
            List<?> list = (List<?>) object;
            Stream<JsonNode> nodeStream = list.stream().map(this::generateNode);
            return JsonArray.fromStream(nodeStream);
        }
        return handleSpecificClass(object);
    }

    private JsonNode handleSpecificClass(Object object) {
        JsonObject jsonObject = JsonFactory.jsonObject();
        Arrays.asList(object.getClass().getFields()).stream()
        .filter(fi -> {
            int modifiers = fi.getModifiers();
            return Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers);
        })
        .forEach(fi -> {
            try {
                jsonObject.withValue(fi.getName(),fi.get(object).toString());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return jsonObject;
    }
}
