package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonNode;

/**
 * A pluggable JSON deserializer for a class. You can override this
 * to control JSON deserialization. Install the class in
 * {@link PojoMapper#registerClassBuilder} or annotate the target class
 * with {@link OverrideMapper}.
 */
public interface JsonPojoBuilder<T> {

    /**
     * Implement to create an object from the JsonNode
     */
    T build(JsonNode jsonObject);
}
