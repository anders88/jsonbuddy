package org.jsonbuddy.pojo;

import org.jsonbuddy.*;

import java.lang.reflect.*;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Convert an object to JSON by mapping fields for any object
 * provided.
 */
public class JsonGenerator {

    /**
     * Recursively serializes the argument as JSON.
     * <ul>
     *   <li>If the argument is a JsonNode the argument is returned.
     *   <li>If it is a String, Number or Boolean, the corresponding JsonNode type is returned.
     *   <li>If it is a Temporal or Enum, the String representation is returned.
     *   <li>If it is a collection, a JsonArray of the elements is returned.
     *   <li>If it implements OverridesJsonGenerator, the custom serialization is called.
     *   <li>If it is an Object, uses reflection to generate a JsonObject of public fields and getters.
     * </ul>
     */
    public static JsonNode generate(Object object) {
        return new JsonGenerator().generateNode(object);
    }

    /** @see #generate(Object) */
    private JsonNode generateNode(Object object) {
        if (object == null) {
            return new JsonNull();
        }
        if (object instanceof JsonNode) {
            return (JsonNode) object;
        }
        if (object instanceof String) {
            return JsonFactory.jsonString((String) object);
        }
        if ((object instanceof Number))  {
            return JsonFactory.jsonNumber(((Number)object));
        }
        if (object instanceof Boolean) {
            return JsonFactory.jsonBoolean((Boolean) object);
        }
        if (object instanceof Enum) {
            return JsonFactory.jsonString(object.toString());
        }
        if (object instanceof Map) {
            Map<Object,Object> map = (Map<Object, Object>) object;
            JsonObject jsonObject = JsonFactory.jsonObject();
            map.entrySet().stream().forEach(entry -> jsonObject.put(entry.getKey().toString(), generateNode(entry.getValue())));

            return jsonObject;
        }
        if (object instanceof Collection) {
            return JsonArray.map((Collection<?>) object, this::generateNode);
        }
        if (object instanceof Temporal) {
            return JsonFactory.jsonString(object.toString());
        }
        if (object instanceof OverridesJsonGenerator) {
            OverridesJsonGenerator overridesJsonGenerator = (OverridesJsonGenerator) object;
            return overridesJsonGenerator.jsonValue();
        }
        return handleSpecificClass(object);
    }

    private static boolean isGetMethod(Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        String methodName = method.getName();
        if (methodName.length() < 4) {
            return false;
        }
        if (!methodName.startsWith("get")) {
            return false;
        }
        if (!Character.isUpperCase(methodName.charAt(3))) {
            return false;
        }
        if (method.getParameterCount() !=0) {
            return false;
        }
        return true;
    }

    private static String getFieldName(Method getMethod) {
        String methodName = getMethod.getName();
        String name = "" + methodName.charAt(3);
        name = name.toLowerCase();
        name = name + methodName.substring(4);
        return name;
    }

    /**
     * Uses reflection to convert the argument to a JsonObject. Each
     * public final field and accessor (getter) is included in the
     * result.
     */
    private JsonObject handleSpecificClass(Object object) {
        JsonObject jsonObject = JsonFactory.jsonObject();
        Arrays.asList(object.getClass().getFields()).stream()
        .filter(fi -> {
            int modifiers = fi.getModifiers();
            return Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers);
        })
        .forEach(fi -> {
            try {
                Object val = fi.get(object);
                JsonNode jsonNode = generateNode(val);
                jsonObject.put(fi.getName(), jsonNode);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        Arrays.asList(object.getClass().getDeclaredMethods()).stream()
                .filter(JsonGenerator::isGetMethod)
                .forEach(method -> {
                    try {
                        Object result = method.invoke(object);
                        JsonNode jsonNode = generateNode(result);
                        jsonObject.put(getFieldName(method), jsonNode);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
        return jsonObject;
    }
}
