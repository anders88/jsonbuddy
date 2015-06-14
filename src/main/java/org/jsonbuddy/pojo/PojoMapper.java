package org.jsonbuddy.pojo;

import org.jsonbuddy.ExceptionUtil;
import org.jsonbuddy.JsonObject;

import java.lang.reflect.Field;
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
            String setterName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
            Method method = clazz.getMethod(setterName, String.class);
            String value = jsonObject.stringValue(key).get();
            method.invoke(result,value);
        };
        return result;
    }

}
