package org.jsonbuddy;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class JsonNode {

    /**
     * The values as json string
     * @return
     */
    public String toJson() {
        StringWriter res = new StringWriter();
        toJson(new PrintWriter(res));
        return res.toString();
    }

    /**
     * Writes this objects as json to the given writer
     * @param printWriter
     */
    public abstract void toJson(PrintWriter printWriter);

    public <T> T as(Class<T> clazz) {
        if (!clazz.isInstance(this)) {
            throw new ClassCastException();
        }
        return (T) this;
    }

    /**
     * Read the value of an attribute from a JsonObject
     * @param key The given key
     * @return The value of the attribute
     * @throws JsonValueNotPresentException If this object is not a Json object, or if a value with the given key does not exist.
     */
    public String requiredString(String key) throws JsonValueNotPresentException {
        throw new JsonValueNotPresentException(String.format("Not supported for class %s",getClass().getSimpleName()));
    }

    public String stringValue() throws JsonValueNotPresentException {
        throw new JsonValueNotPresentException(String.format("Not supported for class %s",getClass().getSimpleName()));
    }

    @Override
    public String toString() {
        return toJson();
    }

    public abstract JsonNode deepClone();
}
