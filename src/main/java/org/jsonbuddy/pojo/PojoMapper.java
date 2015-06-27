package org.jsonbuddy.pojo;

import org.jsonbuddy.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PojoMapper {
    public static <T> T map(JsonObject jsonObject,Class<T> clazz) {
        try {
            return (T) mapit(jsonObject,clazz);
        } catch (Exception e) {
            ExceptionUtil.soften(e);
            return null;
        }
    }

    private static Object mapit(JsonNode jsonNode,Class<?> clazz) throws Exception {
        if (jsonNode instanceof JsonSimpleValue) {
            return ((JsonSimpleValue) jsonNode).javaObjectValue();
        }
        if (jsonNode instanceof JsonArray) {
            return mapArray((JsonArray) jsonNode,clazz);
        }
        JsonObject jsonObject = (JsonObject) jsonNode;
        Object result = clazz.newInstance();
        for (String key : jsonObject.keys()) {
            findField(clazz, jsonObject, result, key);
            if (findSetter(jsonObject, clazz, result, key)) {
                continue;
            }
        };
        return result;
    }

    private static Object mapArray(JsonArray jsonArray, Class<?> clazz) {
        return jsonArray.nodeStream().map(jn -> {
            try {
                return mapit(jn, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    private static boolean findField(Class<?> clazz, JsonObject jsonObject, Object result, String key) throws Exception {
        Field declaredField = null;
        try {
            declaredField = clazz.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            return false;
        }
        JsonNode nodeValue = jsonObject.value(key).get();
        Object value = mapit(nodeValue, computeType(declaredField,nodeValue));
        declaredField.setAccessible(true);
        declaredField.set(result,value);
        declaredField.setAccessible(false);
        return true;
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

    private static boolean findSetter(JsonObject jsonObject, Class<?> clazz, Object instance, String key) throws Exception {
        String setterName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
        Optional<Method> setter = Arrays.asList(clazz.getMethods()).stream()
                .filter(met -> setterName.equals(met.getName()) && met.getParameterCount() == 1)
                .findAny();
        if (!setter.isPresent()) {
            return false;
        }

        Method method = setter.get();
        Class<?> setterClass = method.getParameterTypes()[0];
        Object value = mapit(jsonObject.value(key).get(),setterClass);
        method.invoke(instance,value);
        return true;
    }

}
