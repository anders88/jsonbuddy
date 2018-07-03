package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonNode;

public interface MapitFunction {
    Object mapit(JsonNode jsonNode, Class<?> clazz) throws CanNotMapException;
}
