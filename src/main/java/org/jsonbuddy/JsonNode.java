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

    public String stringValue() throws JsonValueNotPresentException {
        throw new JsonValueNotPresentException(String.format("Not supported for class %s",getClass().getSimpleName()));
    }

    @Override
    public String toString() {
        return toJson();
    }

    public abstract JsonNode deepClone();
}
