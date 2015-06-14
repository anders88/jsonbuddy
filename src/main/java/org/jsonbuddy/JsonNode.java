package org.jsonbuddy;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class JsonNode {
    public String toJson() {
        StringWriter res = new StringWriter();
        toJson(new PrintWriter(res));
        return res.toString();
    }

    public abstract void toJson(PrintWriter printWriter);

    public <T> T as(Class<T> clazz) {
        if (!clazz.isInstance(this)) {
            throw new ClassCastException();
        }
        return (T) this;
    }


    public String requiredString(String key) throws JsonValueNotPresentException {
        throw new JsonValueNotPresentException(String.format("Required key '%s' does not exsist",key));
    }

    public String textValue() {
        throw new JsonValueNotPresentException(String.format("Not supported for class %s",getClass().getSimpleName()));

    }
}
