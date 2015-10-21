package org.jsonbuddy.pojo;

import org.jsonbuddy.*;

import java.lang.reflect.*;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class JsonGenerator {
    public static JsonNode generate(Object object) {
        return new JsonGenerator().generateNode(object);
    }

    private JsonNode generateNode(Object object) {
        if (object == null) {
            return new JsonNull();
        }
        if (object instanceof JsonNode) {
            return (JsonNode) object;
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
        if (object instanceof Boolean) {
            return JsonFactory.jsonBoolean((Boolean) object);
        }
        if (object instanceof Enum) {
            return JsonFactory.jsonText(object.toString());
        }
        if (object instanceof Map) {
            Map<Object,Object> map = (Map<Object, Object>) object;
            JsonObject jsonObject = JsonFactory.jsonObject();
            map.entrySet().stream().forEach(entry -> jsonObject.put(entry.getKey().toString(), generateNode(entry.getValue())));

            return jsonObject;
        }
        if (object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            Stream<JsonNode> nodeStream = collection.stream().map(this::generateNode);
            return JsonArray.fromStream(nodeStream);
        }
        if (object instanceof Temporal) {
            return JsonFactory.jsonText(object.toString());
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

    private JsonNode handleSpecificClass(Object object) {
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
