package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonObject;


/**
 * A mapping rule that overrides the PojoMapping of any number of classes. The implementation must provide the
 * conversion from json to class instance
 */
public interface PojoMappingRule {
    /**
     * Check if this rule is to be used to map the given class
     * @param clazz The class that is about to be mapped
     * @return true if this rule should be used, false otherwize
     */
    boolean isApplicableToClass(Class<?> clazz);

    /**
     * Provide a mapping for the given class
     * @param jsonObject The provided json
     * @param clazz  The class that should be mapped
     * @param mapitfunc A callback function to map nested objets
     * @param <T>
     * @return An implementation mapping the provided json.
     * @throws CanNotMapException If a mapping of the given input can not be performed
     */
    <T> T mapClass(JsonObject jsonObject, Class<T> clazz, MapitFunction mapitfunc) throws CanNotMapException;
}
