package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonNode;

public interface PojoClassMapper<T> {
    T map(JsonNode jsonNode);
}
