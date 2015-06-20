package org.jsonbuddy.pojo;

import org.jsonbuddy.ExceptionUtil;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.JsonSimpleValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
            if (findSetter(jsonObject, clazz, result, key)) {
                continue;
            }
            Field declaredField = clazz.getDeclaredField(key);
            Object value = mapit(jsonObject.value(key).get(),declaredField.getType());
            declaredField.setAccessible(true);
            declaredField.set(result,value);
            declaredField.setAccessible(false);
        };
        return result;
    }

    private static boolean findSetter(JsonObject jsonObject, Class<?> clazz, Object instance, String key) throws IllegalAccessException, InvocationTargetException {
        String setterName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
        Method method = null;
        try {
            method = clazz.getMethod(setterName, String.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
        String value = jsonObject.stringValue(key).get();
        method.invoke(instance,value);
        return true;
    }

}
