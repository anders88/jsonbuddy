package org.jsonbuddy.pojo;

import org.jsonbuddy.*;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
    public static <T> T map(JsonObject jsonObject, Class<T> clazz,PojoMappingRule... options) {
        return new PojoMapper(options).mapToPojo(jsonObject,clazz);
    }

    /**
     * Converts the argument JsonArray into an list of object of the specified class.
     *
     * Each JsonArray element is mapped according to {@link #mapToPojo(JsonObject, Class)}.
     *
     * @throws CanNotMapException if there is no appropriate constructor
     */
    public static <T> List<T> map(JsonArray jsonArray,Class<T> listClazz,PojoMappingRule... options) {
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
            return (T) mapit(jsonObject,clazz);
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


    private Object mapit(JsonNode jsonNode, Class<?> clazz) throws CanNotMapException {
        if (clazz.isAnnotationPresent(OverrideMapper.class)) {
            OverrideMapper[] annotationsByType = clazz.getAnnotationsByType(OverrideMapper.class);
            try {
                return annotationsByType[0].using().newInstance().build(jsonNode);
            } catch (Exception e) {
                throw new CanNotMapException(e);
            }
        }
        if (jsonNode instanceof JsonArray) {
            return mapArray((JsonArray) jsonNode, clazz);
        }
        if (jsonNode instanceof JsonValue) {
            return ((JsonValue) jsonNode).javaObjectValue();
        }

        JsonObject jsonObject = (JsonObject) jsonNode;

        for (PojoMappingRule pojoMappingRule : mappingRules) {
            if (pojoMappingRule.isApplicableToClass(clazz)) {
                return pojoMappingRule.mapClass(jsonObject,clazz,this::mapit);
            }
        }

        if (clazz.isInterface()) {
            throw new CanNotMapException("Can not genereate instance of interfaces, Supply DynamicInterfaceMapper as rule to support this");
        }

        Object result;
        Constructor<?>[] declaredConstructors = Optional.ofNullable(clazz.getDeclaredConstructors()).orElse(new Constructor[0]);
        result = null;
        for (Constructor<?> constructor : declaredConstructors) {
            if (constructor.getParameterCount() == 0) {
                boolean accessible = constructor.isAccessible();
                if (!accessible) {
                    constructor.setAccessible(true);
                }
                try {
                    result = constructor.newInstance();
                } catch (Exception e) {
                    throw new CanNotMapException(e);
                }
                if (!accessible) {
                    constructor.setAccessible(false);

                }
                break;
            }
        }
        if (result == null) {
            throw new CanNotMapException(String.format("Class %s has no default constructor",clazz.getName()));
        }
        for (String key : jsonObject.keys()) {
            try {
                if (tryToSetField(clazz, jsonObject, result, key)) {
                    continue;
                }
                if (tryToSetProperty(jsonObject, clazz, result, key)) {
                    continue;
                }
            } catch (Exception e) {
                throw new CanNotMapException(e);
            }
        }

        return result;
    }

    private Object mapArray(JsonArray jsonArray, Class<?> clazz) {
        return jsonArray.nodeStream().map(jn -> {
            try {
                return mapit(jn, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
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
                optionalValue = Optional.of(mapit(nodeValue, optClass));
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
            Class<?> mappedClass = computeType(declaredField, nodeValue);
            if (List.class.isAssignableFrom(declaredField.getType()) && (nodeValue instanceof JsonArray)) {
                value = mapArray((JsonArray) nodeValue,mappedClass);
            } else {
                value = mapit(nodeValue, mappedClass);
                value = convertIfNecessary(value,declaredField.getType());
            }
        }
        declaredField.setAccessible(true);
        declaredField.set(result,value);
        declaredField.setAccessible(false);
        return true;
    }

    private Map<String, Object> mapAsMap(ParameterizedType genericType, JsonObject nodeValue) throws Exception {
        String typeName = genericType.getActualTypeArguments()[1].getTypeName();
        int genericStart = typeName.indexOf("<");
        if (genericStart != -1) {
            typeName = typeName.substring(genericStart+1,typeName.indexOf(">"));
        }
        Class<?> valueclass = Class.forName(typeName);
        Map<String, Object> result = new HashMap<>();
        for (String key : nodeValue.keys()) {
            result.put(key, mapit(nodeValue.value(key).get(), valueclass));
        }
        return result;
    }

    private Object convertIfNecessary(Object value, Class<?> destinationType) {
        if (value == null) {
            return null;
        }
        if (destinationType.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (value instanceof String && Instant.class.isAssignableFrom(destinationType)) {
            return Instant.parse(value.toString());
        }
        if (value instanceof Number && BigInteger.class.equals(destinationType)) {
            return new BigInteger(value.toString());
        }
        if (value instanceof Number && BigDecimal.class.equals(destinationType)) {
            return new BigDecimal(value.toString());
        }
        if (value instanceof Number && Integer.class.equals(destinationType)) {
            return ((Number) value).intValue();
        }
        if (value instanceof Number && Float.class.equals(destinationType)) {
            return ((Number) value).floatValue();
        }
        if (value instanceof Number && Double.class.equals(destinationType)) {
            return ((Number) value).doubleValue();
        }
        if (Integer.class.equals(destinationType) && (value instanceof String)) {
            return Integer.parseInt((String) value);
        }
        if (Long.class.equals(destinationType) && (value instanceof String)) {
            return Long.parseLong((String) value);
        }
        if (destinationType.isEnum() && (value instanceof String)) {
            String stringValue = (String) value;
            Object[] enumConstants = destinationType.getEnumConstants();
            return Arrays.asList(enumConstants).stream()
                    .filter(o -> stringValue.equals(o.toString()))
                    .findAny().get();
        }
        return value;
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

    private static Class<?> computeType(Field declaredField, JsonNode nodeValue) {
        if (!(nodeValue instanceof JsonArray)) {
            return declaredField.getType();
        }
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
                String typeName = method.getParameters()[0].getParameterizedType().getTypeName();
                Class<?> optClass = Class.forName(typeName.substring("java.util.Optional<".length(), typeName.length() - 1));
                optionalValue = Optional.of(mapit(nodeValue, optClass));
            }
            value = optionalValue;
        } else {
            value = mapit(jsonObject.value(key).get(),setterClass);
            value = convertIfNecessary(value, setterClass);
        }
        method.invoke(instance,value);
        return true;
    }




}
