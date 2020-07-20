package org.jsonbuddy.pojo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonBoolean;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonNumber;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.JsonString;

/**
 * Convert an object to JSON by mapping fields for any object
 * provided.
 */
public class JsonGenerator {

    private final boolean useDeclaringClassAsTemplate;


    protected JsonGenerator(boolean useDeclaringClassAsTemplate) {
        this.useDeclaringClassAsTemplate = useDeclaringClassAsTemplate;
    }

    protected JsonGenerator() {
        this(true);
    }

    /**
     * Recursively serializes the argument as JSON.
     * <ul>
     *   <li>If the argument is a JsonNode the argument is returned.
     *   <li>If it is a String, Number or Boolean, the corresponding JsonNode type is returned.
     *   <li>If it is a Temporal or Enum, the String representation is returned.
     *   <li>If it is a collection, a JsonArray of the elements is returned.
     *   <li>If it implements OverridesJsonGenerator, the custom serialization is called.
     *   <li>If it is an Object, uses reflection to generate a JsonObject of public fields and getters. This method will use the field and method declaration as template for the generation
     * </ul>
     *
     * @param object The object that will be converted to json
     */
    public static JsonNode generate(Object object) {
        return new JsonGenerator(true).generateNode(object,Optional.empty());
    }


    /**
     * Recursively serializes the argument as JSON.
     * <ul>
     *   <li>If the argument is a JsonNode the argument is returned.
     *   <li>If it is a String, Number or Boolean, the corresponding JsonNode type is returned.
     *   <li>If it is a Temporal or Enum, the String representation is returned.
     *   <li>If it is a collection, a JsonArray of the elements is returned.
     *   <li>If it implements OverridesJsonGenerator, the custom serialization is called.
     *   <li>If it is an Object, uses reflection to generate a JsonObject of public fields and getters. This method will use the implementing object as template for the generation
     * </ul>
     *
     * @param object The object that will be converted to json
     *
     *
     */
    public static JsonNode generateUsingImplementationAsTemplate(Object object) {
        return new JsonGenerator(false).generateNode(object,Optional.empty());
    }

    public static JsonNode generateWithSpecifyingClass(Object object, Class<?> classToUse) {
        return new JsonGenerator(true).generateNode(object,Optional.of(classToUse));
    }

    public JsonNode generateNode(Object object, Optional<Type> objectType) {
        if (object == null) {
            return new JsonNull();
        }
        if (object instanceof JsonNode) {
            return (JsonNode) object;
        }
        if (object instanceof String) {
            return new JsonString((String) object);
        }
        if ((object instanceof Number))  {
            return new JsonNumber(((Number)object));
        }
        if (object instanceof Boolean) {
            return new JsonBoolean((Boolean) object);
        }
        if (object instanceof Enum) {
            return new JsonString(object.toString());
        }
        if (object instanceof Map) {
            JsonObject jsonObject = JsonFactory.jsonObject();
            Optional<Type> valueType = objectType.map(this::getElementClass);
            ((Map<?,?>) object).forEach((key, value) -> jsonObject.put(key.toString(), generateNode(value, valueType)));
            return jsonObject;
        }
        if (object instanceof Collection) {
            return JsonArray.map((Collection<?>) object, ob -> generateNode(ob, objectType.map(this::getElementClass)));
        }
        if (object instanceof Stream) {
            return JsonArray.map(((Stream<?>) object).collect(Collectors.toList()), ob -> generateNode(ob, objectType.map(this::getElementClass)));
        }
        if (object.getClass().isArray()) {
            return JsonArray.map(Arrays.asList((Object[])object), ob -> generateNode(ob, objectType.map(this::getElementClass)));
        }
        if (object instanceof Temporal) {
            return new JsonString(object.toString());
        }
        if (object instanceof UUID) {
            return new JsonString(object.toString());
        }
        if (object instanceof OverridesJsonGenerator) {
            OverridesJsonGenerator overridesJsonGenerator = (OverridesJsonGenerator) object;
            return overridesJsonGenerator.jsonValue();
        }
        return handleSpecificClass(object, objectType);
    }

    public static boolean isGetMethod(Method method) {
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
        return method.getParameterCount() == 0;
    }


    /**
     * Uses reflection to convert the argument to a JsonObject. Each
     * public final field and accessor (getter) is included in the
     * result.
     */
    protected JsonObject handleSpecificClass(Object object, Optional<Type> objectType) {
        JsonObject jsonObject = JsonFactory.jsonObject();
        Class<?> theClass = objectType.isPresent() && this.useDeclaringClassAsTemplate ? getRawType(objectType.get()) : object.getClass();
        Arrays.stream(theClass.getFields())
            .filter(fi -> Modifier.isPublic(fi.getModifiers()) && !Modifier.isStatic(fi.getModifiers()))
            .forEach(fi -> {
                try {
                    jsonObject.put(getName(fi),
                            generateNode(fi.get(object), Optional.of(fi.getGenericType())));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        Arrays.stream(theClass.getDeclaredMethods())
                .filter(JsonGenerator::isGetMethod)
                .forEach(method -> {
                    try {
                        jsonObject.put(getName(method),
                                generateNode(method.invoke(object), Optional.of(method.getGenericReturnType())));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
        return jsonObject;
    }

    private Type getElementClass(Type returnType) {
        if (!this.useDeclaringClassAsTemplate) {
            // Shortcut.
            return getRawType(returnType);
        }
        if (!(returnType instanceof ParameterizedType)) {
            return getRawType(returnType);
        }
        ParameterizedType genericReturnType = (ParameterizedType) returnType;
        if (Collection.class.isAssignableFrom(getRawType(genericReturnType))) {
            return getRawType(genericReturnType.getActualTypeArguments()[0]);
        }
        if (Stream.class.isAssignableFrom(getRawType(genericReturnType))) {
            return getRawType(genericReturnType.getActualTypeArguments()[0]);
        }
        if (Map.class.isAssignableFrom(getRawType(genericReturnType))) {
            return getRawType(genericReturnType.getActualTypeArguments()[1]);
        }
        return getRawType(genericReturnType);
    }

    private Class<?> getRawType(Type type) {
        return type instanceof ParameterizedType
                ? getRawType(((ParameterizedType) type).getRawType())
                : (Class<?>) type;
    }

    protected String getName(Field field) {
        return field.getName();
    }

    protected String getName(Method getMethod) {
        String methodName = getMethod.getName();
        String name = "" + methodName.charAt(3);
        name = name.toLowerCase();
        name = name + methodName.substring(4);
        return name;
    }

}
