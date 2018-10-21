package org.jsonbuddy.pojo;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.matcher.ElementMatchers;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonObject;

import java.lang.reflect.Method;
import java.util.*;


/**
 * A mapping rule that can be used to provide a mapping from json to an interface. Bytebuddy (bytebuddy.net) is used to create a runtime instance of the interface.
 * <strong>Important:</strong> Using this class requires you to add the optional dependency byte-buddy to your class path.
 */
public class DynamicInterfaceMapper implements PojoMappingRule {
    private final boolean mapAllGetters;

    private DynamicInterfaceMapper(boolean mapAllGetters) {
        this.mapAllGetters = mapAllGetters;
    }

    public static DynamicInterfaceMapper mapperThatMapsAllGetters() {
        return new DynamicInterfaceMapper(true);
    }

    public DynamicInterfaceMapper() {
        this(false);
    }

    @Override
    public boolean isApplicableToClass(Class<?> clazz, JsonNode jsonNode) {
        return clazz.isInterface() && (jsonNode instanceof JsonObject);
    }


    @Override
    public <T> T mapClass(JsonNode jsonnode, Class<T> clazz, MapitFunction mapitfunc)  throws CanNotMapException {
        JsonObject jsonObject = (JsonObject) jsonnode;
        DynamicType.Builder<T> builder = new ByteBuddy()
                .subclass(clazz);


        Map<String,Method> methodsToMap = new HashMap<>();
        if (mapAllGetters) {
            Arrays.stream(clazz.getDeclaredMethods())
                    .filter(JsonGenerator::isGetMethod)
                    .forEach(method -> {
                        String methodName = method.getName();
                        String key =  "" + Character.toLowerCase(methodName.charAt(3));
                        if (methodName.length() > 4) {
                            key = key + methodName.substring(4);
                        }
                        methodsToMap.put(key,method);
                    });
        } else {
            for (String key : jsonObject.keys()) {
                String getterName = "get" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
                Optional<Method> getter = Arrays.stream(clazz.getMethods())
                        .filter(met -> getterName.equals(met.getName()) && met.getParameterCount() == 0)
                        .findAny();
                if (!getter.isPresent()) {
                    continue;
                }

                Method getterMethod = getter.get();
                methodsToMap.put(key,getterMethod);
            }
        }
        Set<Map.Entry<String, Method>> entries = methodsToMap.entrySet();
        for (Map.Entry<String, Method> methodEntry : entries) {
            String key = methodEntry.getKey();
            Method getterMethod = methodEntry.getValue();

            Object value;
            Optional<JsonNode> jsonValOpt = jsonObject.value(key);
            if (jsonValOpt.isPresent() && (!(jsonValOpt.get() instanceof JsonNull))) {
                value = mapitfunc.mapit(jsonValOpt.get(), getterMethod.getReturnType());
            } else {
                value = null;
            }
            Implementation bbvalue = value != null ? FixedValue.value(value) : FixedValue.nullValue();


            builder = builder.method(ElementMatchers.anyOf(getterMethod))
                    .intercept(bbvalue);
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
