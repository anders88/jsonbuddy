package org.jsonbuddy.pojo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;

/**
 * Support interfaces with dynamic class generation. The pojo will be given
 * values in corresponding getters.
 */
public class DynamicClassMappingRule implements PojoMappingRule {

    @Override
    public boolean isApplicableToClass(Class<?> clazz) {
        return clazz.isInterface();
    }

    @Override
    public <T> JsonPojoBuilder<? extends T> createMapper(Class<T> clazz, PojoMapper pojoMapper) {
        return new DynamicClassPojoBuilder<>(clazz, pojoMapper);
    }


    public static class DynamicClassPojoBuilder<T> implements JsonPojoBuilder<T> {

        private Class<T> clazz;
        private PojoMapper pojoMapper;

        public DynamicClassPojoBuilder(Class<T> clazz, PojoMapper pojoMapper) {
            this.clazz = clazz;
            this.pojoMapper = pojoMapper;
        }

        @Override
        public T build(JsonNode jsonNode) {
            JsonObject jsonObject = (JsonObject) jsonNode;
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

                try {
                    Object value = pojoMapper.mapToValue(jsonObject.value(key).get(), getterMethod.getReturnType());
                    builder = builder.method(ElementMatchers.anyOf(getterMethod))
                        .intercept(FixedValue.value(value));
                } catch (Exception e) {
                    throw ExceptionUtil.soften(e);
                }
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
}
