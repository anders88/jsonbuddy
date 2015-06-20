package org.jsonbuddy.pojo;

import org.jsonbuddy.ExceptionUtil;
import org.jsonbuddy.JsonObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class PojoMapper {
    public static <T> T map(JsonObject jsonObject,Class<T> clazz) {
        try {
            return mapit(jsonObject,clazz);
        } catch (Exception e) {
            ExceptionUtil.soften(e);
            return null;
        }
    }

    private static <T> T mapit(JsonObject jsonObject,Class<T> clazz) throws Exception {
        T result = clazz.newInstance();
        for (String key : jsonObject.keys()) {
            if (findSetter(jsonObject, clazz, result, key)) {
                continue;
            }
            String value = jsonObject.stringValue(key).get();
            Field declaredField = clazz.getDeclaredField(key);
            declaredField.setAccessible(true);
            declaredField.set(result,value);
            declaredField.setAccessible(false);
        };
        return result;
    }

    private static <T> boolean findSetter(JsonObject jsonObject, Class<T> clazz, T instance, String key) throws IllegalAccessException, InvocationTargetException {
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
