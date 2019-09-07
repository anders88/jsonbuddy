package org.jsonbuddy.pojo;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
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


    private final List<PojoMappingRule> mappingRules;

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
        return new PojoMapper(options).mapToPojo(jsonObject,clazz);
    }

    /**
     * Converts the argument JsonArray into an list of object of the specified class.
     *
     * Each JsonArray element is mapped according to {@link #mapToPojo(JsonObject, Class)}.
     *
     * @throws CanNotMapException if there is no appropriate constructor
     */
    public static <T> List<T> map(JsonArray jsonArray,Class<T> listClazz, PojoMappingRule... options) {
        return new PojoMapper(options).mapToPojo(jsonArray,listClazz);
    }


    private PojoMapper(PojoMappingRule[] options) {
        this.mappingRules = options == null ? Collections.emptyList() : Arrays.asList(options);
    }

    public static PojoMapper create(PojoMappingRule... options) {
        return new PojoMapper(options);
    }


    /**
     * Try to convert the argument into the specified class. See {@link #map(JsonObject, Class, PojoMappingRule...)}
     *
     * @return a new object of the specified class
     */
    public <T> T mapToPojo(JsonObject jsonObject, Class<T> clazz) throws CanNotMapException {
        try {
            return (T) mapValue(jsonObject, clazz, null);
        } catch (Exception e) {
            throw ExceptionUtil.soften(e);
        }
    }

    /**
     * Try to convert the argument JsonArray into a list of the specified class.
     * See {@link #map(JsonObject, Class, PojoMappingRule...)} (JsonArray, Class)}
     *
     * @return a new object of the specified class
     */
    public <T> List<T> mapToPojo(JsonArray jsonArray ,Class<T> listClazz) throws CanNotMapException {
        return jsonArray.objects(node -> mapToPojo(node,listClazz));
    }


    private Object mapValue(JsonNode jsonNode, Class<?> clazz, Class<?> elementType) throws CanNotMapException {
        if (clazz.isAnnotationPresent(OverrideMapper.class)) {
            OverrideMapper[] annotationsByType = clazz.getAnnotationsByType(OverrideMapper.class);
            try {
                return annotationsByType[0].using().newInstance().build(jsonNode);
            } catch (Exception e) {
                throw new CanNotMapException(e);
            }
        }
        for (PojoMappingRule pojoMappingRule : mappingRules) {
            if (pojoMappingRule.isApplicableToClass(clazz,jsonNode)) {
                return pojoMappingRule.mapClass(jsonNode, clazz, (n, c) -> mapValue(n, c, null));
            }
        }
        if (jsonNode instanceof JsonArray) {
            return mapArray((JsonArray) jsonNode, clazz, elementType);
        }
        if (jsonNode instanceof JsonValue) {
            return ((JsonValue) jsonNode).javaObjectValue();
        }

        JsonObject jsonObject = (JsonObject) jsonNode;



        if (clazz.isInterface()) {
            throw new CanNotMapException("Can not generate instance of interfaces " + clazz.getName()  + ", Supply DynamicInterfaceMapper as rule to support this");
        }

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
                if (tryToSetField(clazz, jsonObject, result, key)) {
                    continue;
                }
                if (tryToSetProperty(jsonObject, clazz, result, key)) {
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

    private Object mapArray(JsonArray jsonArray, Class<?> collectionType, Class<?> elementType) {
        Stream<Object> stream = jsonArray.nodeStream()
                .map(element -> convertIfNecessary(mapValue(element, elementType, null), elementType));
        if (List.class.isAssignableFrom(collectionType)) {
            return stream.collect(Collectors.toList());
        } else if (Set.class.isAssignableFrom(collectionType)) {
            return stream.collect(Collectors.toSet());
        } else {
            throw new CanNotMapException("Cannot map JsonArray to " + collectionType);
        }
    }

    private boolean tryToSetField(Class<?> clazz, JsonObject jsonObject, Object result, String key) throws Exception {
        Field declaredField = null;
        try {
            declaredField = clazz.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            return false;
        }
        JsonNode nodeValue = jsonObject.value(key).get();
        if (Optional.class.equals(declaredField.getType())) {
            Optional<?> optionalValue;
            if (nodeValue.equals(new JsonNull())) {
                optionalValue = Optional.empty();
            } else {
                String typeName = declaredField.getGenericType().getTypeName();
                Class<?> optClass = Class.forName(typeName.substring("java.util.Optional<".length(), typeName.length() - 1));
                optionalValue = Optional.of(mapValue(nodeValue, optClass, null));
            }

            declaredField.setAccessible(true);
            declaredField.set(result,optionalValue);
            declaredField.setAccessible(false);
            return true;
        }
        Object value;
        OverriddenVal overriddenValue = overriddenValue(declaredField.getType(), nodeValue);
        if (overriddenValue.overridden) {
            value = overriddenValue.value;
        } else if (declaredField.getType().isAssignableFrom(nodeValue.getClass())) {
            value = nodeValue;
        } else if (Map.class.isAssignableFrom(declaredField.getType()) && (nodeValue instanceof JsonObject)) {
            value = mapAsMap((ParameterizedType) declaredField.getGenericType(), (JsonObject) nodeValue);
        } else {
            Class<?> mappedClass = declaredField.getType();
            Class<?> elementType = nodeValue instanceof JsonArray ? computeElementType(declaredField) : null;
            value = mapValue(nodeValue, mappedClass, elementType);
            value = convertIfNecessary(value, declaredField.getType());
        }
        declaredField.setAccessible(true);
        declaredField.set(result,value);
        declaredField.setAccessible(false);
        return true;
    }

    private Map<String, Object> mapAsMap(ParameterizedType genericType, JsonObject nodeValue) throws ClassNotFoundException, CanNotMapException {
        Type type = genericType.getActualTypeArguments()[1];
        Map<String, Object> result = new HashMap<>();
        for (String key : nodeValue.keys()) {
            result.put(key,
                    mapValue(nodeValue.value(key).get(),
                            getContainerClass(type),
                            getElementClass(genericType.getActualTypeArguments()[1])));
        }
        return result;
    }

    private Class<?> getContainerClass(Type type) throws ClassNotFoundException {
        String typeName = type.getTypeName();
        int genericStart = typeName.indexOf("<");
        if (genericStart != -1) {
            typeName = typeName.substring(0, genericStart);
        }
        return Class.forName(typeName);
    }

    private Class<?> getElementClass(Type type) throws ClassNotFoundException {
        String typeName = type.getTypeName();
        int genericStart = typeName.indexOf("<");
        if (genericStart != -1) {
            String elementClassName = typeName.substring(genericStart+1,typeName.indexOf(">"));
            return Class.forName(elementClassName);
        }
        return null;
    }

    private static Map<Class<?>, Function<Number, Object>> numberConverters = new HashMap<>();
    static {
        numberConverters.put(BigInteger.class, n -> new BigInteger(n.toString()));
        numberConverters.put(Long.class, n -> n.longValue());
        numberConverters.put(Long.TYPE, n -> n.longValue());
        numberConverters.put(Integer.class, n -> n.intValue());
        numberConverters.put(Integer.TYPE, n -> n.intValue());
        numberConverters.put(Short.class, n -> n.shortValue());
        numberConverters.put(Short.TYPE, n -> n.shortValue());
        numberConverters.put(Byte.class, n -> n.byteValue());
        numberConverters.put(Byte.TYPE, n -> n.byteValue());
        numberConverters.put(BigDecimal.class, n -> new BigDecimal(n.toString()));
        numberConverters.put(Double.class, n -> n.doubleValue());
        numberConverters.put(Double.TYPE, n -> n.doubleValue());
        numberConverters.put(Float.class, n -> n.floatValue());
        numberConverters.put(Float.TYPE, n -> n.floatValue());
        numberConverters.put(String.class, n -> n.toString());
    }

    private static Map<Class<?>, Function<String, Object>> stringConverters = new HashMap<>();
    static {
        stringConverters.put(UUID.class, s -> UUID.fromString(s));

        stringConverters.put(BigInteger.class, s -> new BigInteger(s));
        stringConverters.put(Long.class, s -> Long.parseLong(s));
        stringConverters.put(Long.TYPE, s -> Long.parseLong(s));
        stringConverters.put(Integer.class, s -> Integer.parseInt(s));
        stringConverters.put(Integer.TYPE, s -> Integer.parseInt(s));
        stringConverters.put(Short.class, s -> Short.parseShort(s));
        stringConverters.put(Short.TYPE, s -> Short.parseShort(s));
        stringConverters.put(Byte.class, s -> Byte.parseByte(s));
        stringConverters.put(Byte.TYPE, s -> Byte.parseByte(s));
        stringConverters.put(BigDecimal.class, s -> new BigDecimal(s));
        stringConverters.put(Double.class, s -> Double.parseDouble(s));
        stringConverters.put(Double.TYPE, s -> Double.parseDouble(s));
        stringConverters.put(Float.class, s -> Float.parseFloat(s));
        stringConverters.put(Float.TYPE, s -> Float.parseFloat(s));
        stringConverters.put(String.class, s -> s);
        stringConverters.put(Boolean.class, s -> Boolean.parseBoolean(s));
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

        throw new JsonConversionException("Cannot convert to " + destinationType + ": " + value);
    }

    protected Object convertEnumValue(Object value, Class<?> destinationType) {
        String stringValue = value.toString();
        Object[] enumConstants = destinationType.getEnumConstants();
        return Arrays.asList(enumConstants).stream()
                .filter(o -> stringValue.equals(o.toString()))
                .findAny()
                .orElseThrow(() -> new CanNotMapException("Illegal value " + value + " for " + destinationType.getSimpleName() + ". Valid options are " + Arrays.asList(enumConstants)));
    }

    private static class OverriddenVal {
        private boolean overridden;
        private Object value;

        public OverriddenVal(boolean ovverridden, Object value) {
            this.overridden = ovverridden;
            this.value = value;
        }
    }

    private static OverriddenVal overriddenValue(Class<?> declaredClass,JsonNode nodValue) {
        if (declaredClass.isAnnotationPresent(OverrideMapper.class)) {
            OverrideMapper[] annotationsByType = declaredClass.getAnnotationsByType(OverrideMapper.class);
            try {
                Object res = annotationsByType[0].using().newInstance().build(nodValue);
                return new OverriddenVal(true,res);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return new OverriddenVal(false,null);
    }

    private static Class<?> computeElementType(Field declaredField) {
        AnnotatedType annotatedType = declaredField.getAnnotatedType();
        AnnotatedParameterizedType para = (AnnotatedParameterizedType) annotatedType;
        Type listType = para.getAnnotatedActualTypeArguments()[0].getType();
        try {
            return Class.forName(listType.getTypeName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean tryToSetProperty(JsonObject jsonObject, Class<?> clazz, Object instance, String key) throws Exception {
        if (key.isEmpty()) {
            return false;
        }
        String setterName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
        Optional<Method> setter = Arrays.asList(clazz.getMethods()).stream()
                .filter(met -> setterName.equals(met.getName()) && met.getParameterCount() == 1)
                .findAny();
        if (!setter.isPresent()) {
            return false;
        }

        Method method = setter.get();
        Class<?> setterClass = method.getParameterTypes()[0];
        Object value;
        OverriddenVal overriddenValue = overriddenValue(setterClass, jsonObject);
        if (overriddenValue.overridden) {
            value = overriddenValue.value;
        } else if (setterClass.isAssignableFrom(jsonObject.getClass())) {
            value = jsonObject;
        } else if (Map.class.isAssignableFrom(setterClass) && (jsonObject.objectValue(key).isPresent())) {
            value = mapAsMap((ParameterizedType) method.getGenericParameterTypes()[0], jsonObject.requiredObject(key));
        } else if (Optional.class.equals(setterClass)) {
            JsonNode nodeValue = jsonObject.value(key).get();
            Optional<?> optionalValue;
            if (nodeValue.equals(new JsonNull())) {
                optionalValue = Optional.empty();
            } else {
                optionalValue = Optional.of(mapValue(nodeValue,
                        getElementClass(method.getParameters()[0].getParameterizedType()),
                        null));
            }
            value = optionalValue;
        } else {
            Type parameterType = method.getGenericParameterTypes()[0];
            value = mapValue(jsonObject.value(key).get(),
                    getContainerClass(parameterType), getElementClass(parameterType));
            value = convertIfNecessary(value, setterClass);
        }
        method.invoke(instance,value);
        return true;
    }




}
