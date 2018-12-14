package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonNode;

/**
 * A pluggable JSON deserializer for a class. You can override this
 * to control JSON deserialization. Annotate the target class
 * with {@link OverrideMapper} to use.
 */
public interface JsonPojoBuilder<T> {

    /**
     * Implement to create an object from the JsonNode
     */
    T build(JsonNode jsonNode);
}
