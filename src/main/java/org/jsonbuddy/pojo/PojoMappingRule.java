package org.jsonbuddy.pojo;

/**
 * A pluggable deserialization rule that can apply to many classes. In particular
 * @see DynamicClassMappingRule which can generate dynamic classes for interfaces.
 */
public interface PojoMappingRule {

    boolean isApplicableToClass(Class<?> clazz);

    <T> JsonPojoBuilder<? extends T> createMapper(Class<T> clazz, PojoMapper pojoMapper);

}
