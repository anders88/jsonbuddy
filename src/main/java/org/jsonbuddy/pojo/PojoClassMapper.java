package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonObject;

public interface PojoClassMapper<T> {
    T map(JsonObject jsonObject);
}
