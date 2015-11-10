package org.jsonbuddy.pojo;

import org.jsonbuddy.*;

import java.lang.reflect.*;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class JsonGenerator {
    public static Object generate(Object object) {
        return new JsonGenerator().generateNode(object);
    }

    private Object generateNode(Object object) {
        if (object == null) {
            return new JsonNull();
        }
        if (object instanceof JsonObject || object instanceof JsonArray) {
            return object;
        }
        if (object instanceof String) {
            return object;
        }
        if (object instanceof Number) {
            return object;
        }
        if (object instanceof Boolean) {
            return object;
        }
        if (object instanceof Enum) {
            return object.toString();
        }
        if (object instanceof Map) {
            Map<Object,Object> map = (Map<Object, Object>) object;
            JsonObject jsonObject = new JsonObject();
            map.entrySet().stream().forEach(entry -> jsonObject.put(entry.getKey().toString(), generateNode(entry.getValue())));

            return jsonObject;
        }
        if (object instanceof Collection) {
            return JsonArray.map((Collection<?>) object, this::generateNode);
        }
        if (object instanceof Temporal) {
            return object.toString();
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

    private Object handleSpecificClass(Object object) {
        JsonObject jsonObject = new JsonObject();
        Arrays.asList(object.getClass().getFields()).stream()
        .filter(fi -> {
            int modifiers = fi.getModifiers();
            return Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers);
        })
        .forEach(fi -> {
            try {
                Object val = fi.get(object);
                Object jsonNode = generateNode(val);
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
                        Object jsonNode = generateNode(result);
                        jsonObject.put(getFieldName(method), jsonNode);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
        return jsonObject;
    }
}
