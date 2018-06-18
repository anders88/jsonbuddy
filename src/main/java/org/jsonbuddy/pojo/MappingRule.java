package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonObject;

public interface MappingRule {
    boolean useThisMapper(Class<?> clazz);
    <T> T mapClass(JsonObject jsonObject, Class<T> clazz, MapitFunction mapitfunc) throws Exception;
}
