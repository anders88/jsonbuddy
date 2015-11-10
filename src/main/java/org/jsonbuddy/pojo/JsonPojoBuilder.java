package org.jsonbuddy.pojo;

public interface JsonPojoBuilder<T> {
    T build(Object jsonObject);
}
