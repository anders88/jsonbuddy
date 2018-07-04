package org.jsonbuddy.pojo;

public interface PojoMappingRule {

    boolean isApplicableToClass(Class<?> clazz);

    <T> JsonPojoBuilder<? extends T> createMapper(Class<T> clazz, PojoMapper pojoMapper);

}
