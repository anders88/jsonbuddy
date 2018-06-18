package org.jsonbuddy.pojo;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.jsonbuddy.JsonObject;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class DynamicInterfaceMapper implements MappingRule {
    @Override
    public boolean useThisMapper(Class<?> clazz) {
        return clazz.isInterface();
    }

    @Override
    public <T> T mapClass(JsonObject jsonObject, Class<T> clazz, MapitFunction mapitfunc) throws Exception {
        DynamicType.Builder<T> builder = new ByteBuddy()
                .subclass(clazz);


        for (String key : jsonObject.keys()) {
            String getterName = "get" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
            Optional<Method> getter = Arrays.stream(clazz.getMethods())
                    .filter(met -> getterName.equals(met.getName()) && met.getParameterCount() == 0)
                    .findAny();
            if (!getter.isPresent()) {
                continue;
            }

            Method getterMethod = getter.get();

            //Object value = mapit(jsonObject.value(key).get(), getterMethod.getReturnType());
            Object value = mapitfunc.mapit(jsonObject.value(key).get(), getterMethod.getReturnType());
            builder = builder.method(ElementMatchers.anyOf(getterMethod))
                    .intercept(FixedValue.value(value));
        }

        Class<? extends T> loaded = builder
                .make()
                .load(clazz.getClassLoader())
                .getLoaded();

        return createInstance(loaded);
    }

    private static <T> T createInstance(Class<? extends T> loaded) {
        try {
            return loaded.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
