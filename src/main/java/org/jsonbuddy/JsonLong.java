package org.jsonbuddy;

import java.io.PrintWriter;
import java.util.Objects;

public class JsonLong extends JsonSimpleValue {

    private final long value;

    public JsonLong(long value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return Long.toString(value);
    }

    @Override
    public Object javaObjectValue() {
        if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE){
            int res = (int) value;
            return res;
        }
        return value;
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append(stringValue());
    }

    @Override
    public JsonLong deepClone() {
        return this;
    }

    public long longValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonLong)) return false;
        JsonLong jsonLong = (JsonLong) o;
        return Objects.equals(value, jsonLong.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
