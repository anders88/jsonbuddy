package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonNode;

public interface JsonPojoBuilder<T> {
    T build(JsonNode jsonObject);
}
