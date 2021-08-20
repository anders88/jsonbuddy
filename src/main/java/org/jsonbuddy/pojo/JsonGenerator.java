package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonBoolean;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonNumber;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.JsonString;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convert an object to JSON by mapping fields for any object
 * provided.
 */
public class JsonGenerator {

    public static final Function<String, String> UNDERSCORE_TRANSFORMER = 
            s -> s.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    
    private final boolean useDeclaringClassAsTemplate;
    private Function<String, String> nameTransformer = Function.identity();

    public JsonGenerator(boolean useDeclaringClassAsTemplate) {
        this.useDeclaringClassAsTemplate = useDeclaringClassAsTemplate;
    }

    public JsonGenerator() {
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
        return new JsonGenerator(true).generateNode(object);
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
        return new JsonGenerator(false).generateNode(object);
    }

    public static JsonNode generateWithSpecifyingClass(Object object, Class<?> classToUse) {
        return new JsonGenerator(true).generateNode(object, Optional.of(classToUse));
    }

    private final Map<Class<?>, Function<Object, JsonNode>> converters = new HashMap<>();
    {
        addConverter(String.class, JsonString::new);
        addConverter(Number.class, JsonNumber::new);
        addConverter(Boolean.class, JsonBoolean::new);
        addConverter(Enum.class, o -> new JsonString(o.toString()));
        addConverter(UUID.class, o -> new JsonString(o.toString()));
        addConverter(URL.class, o -> new JsonString(o.toString()));
        addConverter(URI.class, o -> new JsonString(o.toString()));
        addConverter(InetAddress.class, o -> new JsonString(o.getHostName()));
        addConverter(Temporal.class, o -> new JsonString(o.toString()));
        addConverter(Optional.class, o -> (JsonNode) o.map(this::generateNode).orElse(new JsonNull()));
    }

    public <T> void addConverter(Class<T> sourceClass, Function<T, JsonNode> converter) {
        //noinspection unchecked
        converters.put(sourceClass,  (Function<Object, JsonNode>) converter);
    }

    public JsonGenerator withNameTransformer(Function<String, String> nameTransformer) {
        this.nameTransformer = nameTransformer;
        return this;
    }

    public JsonNode generateNode(Object object) {
        return generateNode(object, Optional.empty());
    }

    public JsonNode generateNode(Object object, Optional<Type> objectType) {
        if (object == null) {
            return new JsonNull();
        }
        if (object instanceof JsonNode) {
            return (JsonNode) object;
        }
        for (Class<?> converterClass : converters.keySet()) {
            if (converterClass.isAssignableFrom(object.getClass())) {
                return converters.get(converterClass).apply(object);
            }
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
        if (object instanceof OverridesJsonGenerator) {
            OverridesJsonGenerator overridesJsonGenerator = (OverridesJsonGenerator) object;
            return overridesJsonGenerator.jsonValue();
        }
        return handleSpecificClass(object, objectType);
    }

    public static boolean isGetMethod(Method method) {
        if (!Modifier.isPublic(method.getModifiers()) || method.getDeclaringClass() == Object.class) {
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
        for (Field field : theClass.getFields()) {
            if (Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                try {
                    jsonObject.put(getName(field),
                            generateNode(field.get(object), Optional.of(field.getGenericType())));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (Method method : theClass.getMethods()) {
            if (isGetMethod(method)) {
                try {
                    jsonObject.put(
                            getName(method),
                            generateNode(method.invoke(object), Optional.of(method.getGenericReturnType()))
                    );
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return jsonObject;
    }

    private Type getElementClass(Type type) {
        if (!this.useDeclaringClassAsTemplate) {
            // Shortcut.
            return getRawType(type);
        }
        if (!(type instanceof ParameterizedType)) {
            return getRawType(type);
        }
        ParameterizedType genericReturnType = (ParameterizedType) type;
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
        return transformName(field.getName());
    }

    protected String getName(Method getMethod) {
        String methodName = getMethod.getName();
        String name = "" + methodName.charAt(3);
        name = name.toLowerCase();
        name = name + methodName.substring(4);
        return transformName(name);
    }

    protected String transformName(String name) {
        return nameTransformer.apply(name);
    }

}
