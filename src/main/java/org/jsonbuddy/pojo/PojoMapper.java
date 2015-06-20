package org.jsonbuddy.pojo;

import org.jsonbuddy.ExceptionUtil;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.JsonSimpleValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

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
            return ((JsonSimpleValue) jsonNode).stringValue();
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

    private static boolean findField(Class<?> clazz, JsonObject jsonObject, Object result, String key) throws Exception {
        Field declaredField = null;
        try {
            declaredField = clazz.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            return false;
        }
        Object value = mapit(jsonObject.value(key).get(),declaredField.getType());
        declaredField.setAccessible(true);
        declaredField.set(result,value);
        declaredField.setAccessible(false);
        return true;
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
