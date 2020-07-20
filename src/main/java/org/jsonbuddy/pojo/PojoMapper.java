package org.jsonbuddy.pojo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonConversionException;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.JsonValue;

/**
 * Deserializes a JsonObject or JsonArray into plain Java objects by setting
 * fields and calling setters on the target object.
 */
public class PojoMapper {


    public static PojoMapper create(PojoMappingRule... options) {
        return new PojoMapper(options);
    }

    /**
     * Converts the argument JsonObject into an object of the specified class.
     *
     * <ul>
     *   <li>If the class is annotated with an {@link OverrideMapper}, this deserializer is used
     *   <li>Otherwise, try to instantiate the class by reflection, set fields and call setters
     * </ul>
     *
     * @param options Zero one or more mapping rules that overrides default behaviour. Each of the supplied
     *                rules are checked in order and used if it matches class.
     *
     * @throws CanNotMapException if there is no appropriate constructor
     */
    public static <T> T map(JsonObject jsonObject, Class<T> clazz, PojoMappingRule... options) {
        return create(options).mapToPojo(jsonObject,clazz);
    }

    /**
     * Converts the argument JsonArray into an list of object of the specified class.
     *
     * Each JsonArray element is mapped according to {@link #mapToPojo(JsonNode, Type)}.
     *
     * @throws CanNotMapException if there is no appropriate constructor
     */
    public static <T> List<T> map(JsonArray jsonArray, Class<T> listClazz, PojoMappingRule... options) {
        return create(options).mapToPojo(jsonArray,listClazz);
    }

    /**
     * Converts the argument JsonNode into any parameterized or plain type of the specified type.
     * The JsonNode is mapped according to {@link #mapToPojo(JsonNode, Type)}.
     *
     * @throws CanNotMapException if there is no appropriate constructor
     */
    public static <T> T mapType(JsonNode json, Type type, PojoMappingRule... options) {
        return new PojoMapper(options).mapToPojo(json, type);
    }

    private final List<PojoMappingRule> mappingRules;

    protected PojoMapper(PojoMappingRule[] options) {
        this.mappingRules = options == null ? Collections.emptyList() : Arrays.asList(options);
    }

    /**
     * Converts the argument JsonNode into an object of the specified type.
     *
     * <ul>
     *   <li>If the type is a collection, map each element according to #mapToPojo
     *   (only supports {@link List} and {@link Set})
     *   <li>If the type is subclass of JsonNode, the json is returned directly
     *   <li>If the class is annotated with an {@link OverrideMapper}, this deserializer is used
     *   <li>Otherwise, try to instantiate the class by reflection, set fields and call setters
     * </ul>
     *
     * @throws CanNotMapException if there is no appropriate constructor
     */
    public <T> T mapToPojo(JsonNode json, Type type) {
        try {
            //noinspection unchecked
            return (T) mapValue(json, type);
        } catch (Exception e) {
            throw ExceptionUtil.soften(e);
        }
    }

    /**
     * Try to convert the argument JsonArray into a list of the specified class.
     * See {@link #map(JsonObject, Class, PojoMappingRule...)} (JsonArray, Class)}.
     *
     * @return a new object of the specified class
     */
    public <T> List<T> mapToPojo(JsonArray jsonArray, Type listClazz) throws CanNotMapException {
        return mapToPojo(jsonArray, List.class, listClazz);
    }

    /**
     * Try to convert the argument JsonArray into a collecton of the specified class.
     *
     * @param collectionType The target collection type, only {@link Collection},
     *                       {@link Stream}, {@link List} and {@link Set} are supported
     * @param elementType The contents of the collection, mapped with the rules of {@link #mapToPojo(JsonNode, Type)}
     * @return a new collection of the specified class with elements of the specified class
     */
    @SuppressWarnings("unchecked")
    public <T> T mapToPojo(JsonArray jsonArray, Type collectionType, Type elementType) {
        if (getClassType(collectionType) == JsonArray.class) {
            return (T) jsonArray;
        }
        Stream<?> stream = jsonArray.nodeStream().map(element -> mapValue(element, elementType));
        if (getClassType(collectionType) == Stream.class) {
            return (T)stream;
        } else if (getClassType(collectionType) == Set.class) {
            return (T)stream.collect(Collectors.toSet());
        } else if (getClassType(collectionType) == List.class || collectionType == Collection.class) {
            return (T)stream.collect(Collectors.toList());
        } else {
            throw new CanNotMapException("Cannot map JsonArray to " + collectionType);
        }
    }

    /**
     * Converts the argument JsonObject into an Map. Each JsonObject property value is mapped according
     * to {@link #mapToPojo(JsonNode, Type)}.
     *
     * @param nodeValue The JsonObject to be converted
     * @param mapType The (potentially generic) type of the Map. If type parameters are given,
     *                each value element is converted according to {@link #mapToPojo(JsonNode, Type)}
     */
    public Map<String, Object> mapToMap(JsonObject nodeValue, Type mapType) throws CanNotMapException {
        ParameterizedType genericType = (ParameterizedType) mapType;
        Map<String, Object> result = new HashMap<>();
        for (String key : nodeValue.keys()) {
            result.put(key, mapValue(nodeValue.requiredValue(key), genericType.getActualTypeArguments()[1]));
        }
        return result;
    }

    private Object mapValue(JsonNode jsonNode, Type type) throws CanNotMapException {
        Class<?> clazz = getClassType(type);
        if (clazz.isAnnotationPresent(OverrideMapper.class)) {
            OverrideMapper[] annotationsByType = clazz.getAnnotationsByType(OverrideMapper.class);
            try {
                return annotationsByType[0].using().getConstructor().newInstance().build(jsonNode);
            } catch (Exception e) {
                throw new CanNotMapException(e);
            }
        }
        if (clazz.isAssignableFrom(jsonNode.getClass())) {
            return jsonNode;
        }
        if (clazz == Optional.class) {
            if (jsonNode instanceof JsonNull) {
                return Optional.empty();
            } else {
                return Optional.of(mapValue(jsonNode, getElementClass(type)));
            }
        }
        if (Map.class.isAssignableFrom(clazz) && (jsonNode instanceof JsonObject)) {
            return mapToMap((JsonObject) jsonNode, type);
        }
        if (jsonNode instanceof JsonArray) {
            return mapToPojo((JsonArray) jsonNode, type, getElementClass(type));
        }
        for (PojoMappingRule pojoMappingRule : mappingRules) {
            if (pojoMappingRule.isApplicableToClass(clazz, jsonNode)) {
                return pojoMappingRule.mapClass(jsonNode, clazz, this::mapValue);
            }
        }

        if (jsonNode instanceof JsonValue) {
            return convertIfNecessary(((JsonValue) jsonNode).javaObjectValue(), clazz);
        }

        JsonObject jsonObject = (JsonObject) jsonNode;
        if (clazz.isInterface()) {
            throw new CanNotMapException("Can not generate instance of interfaces " + clazz.getName()  + ", Supply DynamicInterfaceMapper as rule to support this");
        }
        return mapToJavaObject(jsonObject, clazz);
    }

    private Object convertIfNecessary(Object value, Class<?> destinationType) {
        if (value == null) {
            return null;
        }
        if (destinationType.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (value instanceof Boolean && destinationType == Boolean.TYPE) {
            return value;
        }
        if (value instanceof Number && numberConverters.containsKey(destinationType)) {
            return numberConverters.get(destinationType).apply((Number)value);
        }
        if (value instanceof CharSequence && stringConverters.containsKey(destinationType)) {
            return stringConverters.get(destinationType).apply(value.toString());
        }
        if (destinationType.isEnum() && (value instanceof CharSequence)) {
            return convertEnumValue(value, destinationType);
        }
        if (Temporal.class.isAssignableFrom(destinationType)) {
            return convertTemporal(value, destinationType);
        }

        throw new JsonConversionException("Cannot convert to " + destinationType + ": " + value);
    }

    private Object convertTemporal(Object value, Class<?> destinationType) {
        try {
            Method parseMethod = destinationType.getMethod("parse", CharSequence.class);
            return parseMethod.invoke(null, value.toString());
        } catch (NoSuchMethodException|SecurityException|IllegalAccessException e) {
            throw new CanNotMapException("Could not find " + destinationType.getName() + "::parse");
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException)e.getTargetException();
            } else {
                throw new CanNotMapException("Cannot map to " + destinationType + ": " + e);
            }
        }
    }

    protected Object convertEnumValue(Object value, Class<?> destinationType) {
        String stringValue = value.toString();
        Object[] enumConstants = destinationType.getEnumConstants();
        return Arrays.stream(enumConstants)
                .filter(o -> stringValue.equals(o.toString()))
                .findAny()
                .orElseThrow(() -> new CanNotMapException("Illegal value " + value + " for " + destinationType.getSimpleName() + ". Valid options are " + Arrays.asList(enumConstants)));
    }

    protected Object mapToJavaObject(JsonObject jsonObject, Class<?> clazz) {
        Constructor<?> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException | SecurityException e1) {
            throw new CanNotMapException(String.format("Class %s has no default constructor",clazz.getName()));
        }
        constructor.setAccessible(true);
        Object result;
        try {
            result = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new CanNotMapException(e.getMessage());
        }
        for (String key : jsonObject.keys()) {
            try {
                if (tryToSetField(clazz, result, key, jsonObject)) {
                    continue;
                }
                if (tryToSetProperty(clazz, result, key, jsonObject)) {
                    continue;
                }
            } catch (CanNotMapException e) {
                throw e;
            } catch (JsonConversionException e) {
                throw new CanNotMapException("Cannot set " + key + ": " + e.getMessage());
            } catch (Exception e) {
                throw new CanNotMapException(e);
            }
        }
        return result;
    }

    protected boolean tryToSetField(Class<?> clazz, Object instance, String key, JsonObject jsonObject) throws Exception {
        Field declaredField;
        try {
            declaredField = clazz.getDeclaredField(fieldName(key));
        } catch (NoSuchFieldException e) {
            return false;
        }
        Object value = mapValue(jsonObject.requiredValue(key), declaredField.getGenericType());
        declaredField.setAccessible(true);
        declaredField.set(instance, value);
        declaredField.setAccessible(false);
        return true;
    }

    protected boolean tryToSetProperty(Class<?> clazz, Object instance, String key, JsonObject jsonObject) throws Exception {
        String setterName = setterName(key);
        Optional<Method> setter = Arrays.stream(clazz.getMethods())
                .filter(met -> setterName.equals(met.getName()) && met.getParameterCount() == 1)
                .findAny();
        if (!setter.isPresent()) {
            return false;
        }
        setter.get().invoke(instance, mapValue(jsonObject.requiredValue(key), setter.get().getGenericParameterTypes()[0]));
        return true;
    }

    protected Class<?> getClassType(Type type) {
        if (type instanceof ParameterizedType) {
            return getClassType(((ParameterizedType)type).getRawType());
        }
        return (Class<?>) type;
    }

    protected Class<?> getElementClass(Type type) {
        if (type instanceof ParameterizedType) {
            return getClassType(((ParameterizedType)type).getActualTypeArguments()[0]);
        }
        return null;
    }

    protected String setterName(String key) {
        String fieldName = fieldName(key);
        return "set" + (key.length() > 0 ? Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) : "");
    }

    protected String fieldName(String key) {
        return fromUnderscoreToCamelCase(key);
    }

    protected String fromUnderscoreToCamelCase(String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            if (i < key.length() -1 && key.charAt(i) == '_') {
                result.append(Character.toUpperCase(key.charAt(++i)));
            } else {
                result.append(key.charAt(i));
            }
        }
        return result.toString();
    }

    private static final Map<Class<?>, Function<Number, Object>> numberConverters = new HashMap<>();
    static {
        numberConverters.put(BigInteger.class, n -> new BigInteger(n.toString()));
        numberConverters.put(Long.class, Number::longValue);
        numberConverters.put(Long.TYPE, Number::longValue);
        numberConverters.put(Integer.class, Number::intValue);
        numberConverters.put(Integer.TYPE, Number::intValue);
        numberConverters.put(Short.class, Number::shortValue);
        numberConverters.put(Short.TYPE, Number::shortValue);
        numberConverters.put(Byte.class, Number::byteValue);
        numberConverters.put(Byte.TYPE, Number::byteValue);
        numberConverters.put(BigDecimal.class, n -> new BigDecimal(n.toString()));
        numberConverters.put(Double.class, Number::doubleValue);
        numberConverters.put(Double.TYPE, Number::doubleValue);
        numberConverters.put(Float.class, Number::floatValue);
        numberConverters.put(Float.TYPE, Number::floatValue);
        numberConverters.put(String.class, Object::toString);
    }

    private static final Map<Class<?>, Function<String, Object>> stringConverters = new HashMap<>();
    static {
        stringConverters.put(UUID.class, UUID::fromString);
        stringConverters.put(BigInteger.class, BigInteger::new);
        stringConverters.put(Long.class, Long::parseLong);
        stringConverters.put(Long.TYPE, Long::parseLong);
        stringConverters.put(Integer.class, Integer::parseInt);
        stringConverters.put(Integer.TYPE, Integer::parseInt);
        stringConverters.put(Short.class, Short::parseShort);
        stringConverters.put(Short.TYPE, Short::parseShort);
        stringConverters.put(Byte.class, Byte::parseByte);
        stringConverters.put(Byte.TYPE, Byte::parseByte);
        stringConverters.put(BigDecimal.class, BigDecimal::new);
        stringConverters.put(Double.class, Double::parseDouble);
        stringConverters.put(Double.TYPE, Double::parseDouble);
        stringConverters.put(Float.class, Float::parseFloat);
        stringConverters.put(Float.TYPE, Float::parseFloat);
        stringConverters.put(String.class, s -> s);
        stringConverters.put(Boolean.class, Boolean::parseBoolean);
    }
}
