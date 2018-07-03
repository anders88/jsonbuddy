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
        toJson(new PrintWriter(res), "", "");
        return res.toString();
    }

    public String toIndentedJson(String indentationAmount) {
        StringWriter res = new StringWriter();
        toJson(new PrintWriter(res), "", indentationAmount);
        return res.toString();
    }


    /**
     * Writes this objects as JSON to the given writer
     */
    public abstract void toJson(PrintWriter printWriter, String currentIntentation, String indentationAmount);

    public void toJson(PrintWriter printWriter) {
        toJson(printWriter,"","");
    }

    public String stringValue() throws JsonValueNotPresentException {
        throw new JsonValueNotPresentException(String.format("Not supported for class %s",getClass().getSimpleName()));
    }

    @Override
    public String toString() {
        return toJson();
    }

    public abstract JsonNode deepClone();

    /**
     * Check if this node is an array
     * @return true if this is a JsonArray, false otherwise
     */
    public boolean isArray() {
        return (this instanceof JsonArray);
    }

    /**
     * Check if this node is an object
     * @return true if this is a JsonObject, false otherwise
     */
    public boolean isObject() {
        return (this instanceof JsonObject);
    }
}
