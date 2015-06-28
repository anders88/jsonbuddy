package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.StreamSupport;

public class JsonGenerator {
    public static JsonNode generate(Object object) {
        return new JsonGenerator().generateNode(object);
    }

    private JsonNode generateNode(Object object) {
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
