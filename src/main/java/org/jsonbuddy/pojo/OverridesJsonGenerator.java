package org.jsonbuddy.pojo;

import org.jsonbuddy.JsonNode;

/**
 * Implement this interface on an object that should have custom
 * JSON serialization. PojoMapper will invoke {@link #jsonValue()}
 * during serialization
 */
public interface OverridesJsonGenerator {

    /**
     * Returns this object as a JSON object
     */
    JsonNode jsonValue();
}
