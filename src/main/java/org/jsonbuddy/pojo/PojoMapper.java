package org.jsonbuddy.pojo;

import org.jsonbuddy.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class PojoMapper {

    private static Map<Class<?>,JsonPojoBuilder<?>> globalPojoBuilders = new HashMap<>();

    private Map<Class<?>,JsonPojoBuilder<?>> pojoBuilders = new HashMap<>();

    public static <T> T map(JsonObject jsonObject,Class<T> clazz) {
        return new PojoMapper().mapToPojo(jsonObject,clazz);
    }

    public static <T> List<T> map(JsonArray jsonArray,Class<T> listClazz) {
        return new PojoMapper().mapToPojo(jsonArray,listClazz);
    }

    private PojoMapper() {
        pojoBuilders.putAll(globalPojoBuilders);
    }

    public static PojoMapper create() {
        return new PojoMapper();
    }


    public <T> PojoMapper registerClassBuilder(Class<T> clazz,JsonPojoBuilder<T> jsonPojoBuilder) {
        pojoBuilders.put(clazz, jsonPojoBuilder);
        return this;
    }

    public static <T> void registerGlobalClassBuilder(Class<T> clazz,JsonPojoBuilder<T> jsonPojoBuilder) {
        globalPojoBuilders.put(clazz,jsonPojoBuilder);
    }

    public <T> T mapToPojo(JsonObject jsonObject,Class<T> clazz) {
        try {
            return (T) mapit(jsonObject,clazz);
        } catch (Exception e) {
            ExceptionUtil.soften(e);
            return null;
        }
    }

    public  <T> List<T> mapToPojo(JsonArray jsonArray,Class<T> listClazz) {
        return jsonArray.nodeStream()
                .filter(node -> node instanceof JsonObject)
                .map(node -> mapToPojo((JsonObject) node,listClazz))
                .collect(Collectors.toList());
    }

    private Object mapit(JsonNode jsonNode,Class<?> clazz) throws Exception {
        if (jsonNode instanceof JsonSimpleValue) {
            return ((JsonSimpleValue) jsonNode).javaObjectValue();
        }
        if (jsonNode instanceof JsonArray) {
            return mapArray((JsonArray) jsonNode,clazz);
        }
        JsonObject jsonObject = (JsonObject) jsonNode;
        JsonPojoBuilder<?> jsonPojoBuilder = pojoBuilders.get(clazz);
        if (jsonPojoBuilder != null) {
            return jsonPojoBuilder.build(jsonObject);
        }

        Object result;
        if (clazz.isAnnotationPresent(OverrideMapper.class)) {
            OverrideMapper[] annotationsByType = clazz.getAnnotationsByType(OverrideMapper.class);
            result = annotationsByType[0].using().newInstance().map(jsonObject);
        } else {
            result = clazz.newInstance();
            for (String key : jsonObject.keys()) {
                if (findField(clazz, jsonObject, result, key)) {
                    continue;
                }
                if (findSetter(jsonObject, clazz, result, key)) {
                    continue;
                }
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

    private boolean findField(Class<?> clazz, JsonObject jsonObject, Object result, String key) throws Exception {
        Field declaredField = null;
        try {
            declaredField = clazz.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            return false;
        }
        JsonNode nodeValue = jsonObject.value(key).get();
        Object value;
        Optional<Object> overriddenValue = overriddenValue(declaredField.getType(), nodeValue);
        if (overriddenValue.isPresent()) {
            value = overriddenValue.get();
        } else if (declaredField.getType().isAssignableFrom(nodeValue.getClass())) {
            value = nodeValue;
        } else {
            value = mapit(nodeValue, computeType(declaredField, nodeValue));
        }
        declaredField.setAccessible(true);
        declaredField.set(result,value);
        declaredField.setAccessible(false);
        return true;
    }

    private static Optional<Object> overriddenValue(Class declaredClass,JsonNode nodValue) {
        if (declaredClass.isAnnotationPresent(OverrideMapper.class)) {
            OverrideMapper[] annotationsByType = (OverrideMapper[]) declaredClass.getAnnotationsByType(OverrideMapper.class);
            try {
                Object res = annotationsByType[0].using().newInstance().map(nodValue);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
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

    private boolean findSetter(JsonObject jsonObject, Class<?> clazz, Object instance, String key) throws Exception {
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
        Optional<Object> overriddenValue = overriddenValue(setterClass, jsonObject);
        if (overriddenValue.isPresent()) {
            value = overriddenValue.get();
        } else if (setterClass.isAssignableFrom(jsonObject.getClass())) {
            value = jsonObject;
        } else {
            value = mapit(jsonObject.value(key).get(),setterClass);
        }
        method.invoke(instance,value);
        return true;
    }

}
