package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonString;

/**
 * Used to Pojo Map enums. Enum.valueOf is used to map String to Enum instance
 */
public class EnumMapper implements PojoMappingRule {
    @Override
    public boolean isApplicableToClass(Class<?> clazz, JsonNode jsonNode) {
        return clazz.isEnum() && (jsonNode instanceof JsonString);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T mapClass(JsonNode jsonNode, Class<T> clazz, MapitFunction mapitfunc) throws CanNotMapException {
        String value = jsonNode.stringValue();
        @SuppressWarnings("unchecked") Enum anEnum = Enum.valueOf((Class<Enum>) clazz, value);
        return (T) anEnum;
    }
}
