package org.jsonbuddy;

public interface JsonPojoBuilder<T> {
    public T build(JsonObject jsonObject);

}
