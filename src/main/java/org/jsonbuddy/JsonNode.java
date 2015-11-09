package org.jsonbuddy;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Common superclass of all JSON elements. A JsonNode can be
 * complex (JsonArray, JsonObject) or value (string, number, boolean).
 */
public abstract class JsonNode {

    /**
     * The value as a JSON string
     */
    public String toJson() {
        StringWriter res = new StringWriter();
        toJson(new PrintWriter(res));
        return res.toString();
    }

    /**
     * Writes this objects as JSON to the given writer
     */
    public abstract void toJson(PrintWriter printWriter);

    public String stringValue() throws JsonValueNotPresentException {
        throw new JsonValueNotPresentException(String.format("Not supported for class %s",getClass().getSimpleName()));
    }

    @Override
    public String toString() {
        return toJson();
    }

    public abstract JsonNode deepClone();
}
