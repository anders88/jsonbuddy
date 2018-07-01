package org.jsonbuddy.pojo;

import org.jsonbuddy.*;

import java.lang.reflect.*;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * Convert an object to JSON by mapping fields for any object
 * provided.
 */
public class JsonGenerator {

    private final boolean useDeclaringClassAsTemplate;


    public JsonGenerator(boolean useDeclaringClassAsTemplate) {
        this.useDeclaringClassAsTemplate = useDeclaringClassAsTemplate;
    }

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
     *
     * @param object The object that will be converted to json
     *
     *
     */
    public static JsonNode generate(Object object) {
        return new JsonGenerator(true).generateNode(object,Optional.empty());
    }

    public static JsonNode generateUsingImplementationAsTemplate(Object object) {
        return new JsonGenerator(false).generateNode(object,Optional.empty());
    }

    public static JsonNode generateWithSpecifyingClass(Object object,Class classToUse) {
        return new JsonGenerator(true).generateNode(object,Optional.of(classToUse));
    }

    private JsonNode generateNode(Object object, Optional<Class> declaringClass) {
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
            map.entrySet().stream().forEach(entry -> jsonObject.put(entry.getKey().toString(), generateNode(entry.getValue(),declaringClass)));

            return jsonObject;
        }
        if (object instanceof Collection) {
            return JsonArray.map((Collection<?>) object, ob -> generateNode(ob,declaringClass));
        }
        if (object.getClass().isArray()) {
            return JsonArray.map(Arrays.asList((Object[])object), ob -> generateNode(ob,declaringClass));
        }
        if (object instanceof Temporal) {
            return JsonFactory.jsonString(object.toString());
        }
        if (object instanceof OverridesJsonGenerator) {
            OverridesJsonGenerator overridesJsonGenerator = (OverridesJsonGenerator) object;
            return overridesJsonGenerator.jsonValue();
        }
        return handleSpecificClass(object,declaringClass);
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
    private JsonObject handleSpecificClass(Object object,Optional<Class> declaringClass) {
        JsonObject jsonObject = JsonFactory.jsonObject();
        Class<?> theClass = declaringClass.isPresent() && this.useDeclaringClassAsTemplate ? declaringClass.get() : object.getClass();
        Arrays.asList(theClass.getFields()).stream()
        .filter(fi -> {
            int modifiers = fi.getModifiers();
            return Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers);
        })
        .forEach(fi -> {
            try {
                Object val = fi.get(object);

                Class<?> type = fi.getType();
                AnnotatedType annotatedType = fi.getAnnotatedType();
                type = overrideReturnType(type,annotatedType);
                JsonNode jsonNode = generateNode(val,Optional.of(type));
                jsonObject.put(fi.getName(), jsonNode);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        Arrays.asList(theClass.getDeclaredMethods()).stream()
                .filter(JsonGenerator::isGetMethod)
                .forEach(method -> {
                    try {
                        Class returnType = method.getReturnType();
                        AnnotatedType annotatedType = method.getAnnotatedReturnType();

                        returnType = overrideReturnType(returnType, annotatedType);
                        Object result = method.invoke(object);
                        JsonNode jsonNode = generateNode(result,Optional.of(returnType));
                        jsonObject.put(getFieldName(method), jsonNode);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
        return jsonObject;
    }

    private Class overrideReturnType(Class returnType, AnnotatedType in) {
        if (!this.useDeclaringClassAsTemplate) {
            // Shortcut.
            return returnType;
        }
        if (!(in instanceof AnnotatedParameterizedType)) {
            return returnType;
        }
        AnnotatedParameterizedType annotatedType = (AnnotatedParameterizedType) in;
        if (Collection.class.isAssignableFrom(returnType)) {
            Type listType = annotatedType.getAnnotatedActualTypeArguments()[0].getType();
            try {
                return Class.forName(listType.getTypeName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        if (Map.class.isAssignableFrom(returnType)) {
            Type valuetype = annotatedType.getAnnotatedActualTypeArguments()[1].getType();
            try {
                return Class.forName(valuetype.getTypeName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return returnType;
    }
}
