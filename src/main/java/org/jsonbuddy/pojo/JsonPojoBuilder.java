package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonObject;

public interface JsonPojoBuilder<T> {
    T build(JsonObject jsonObject);
}
