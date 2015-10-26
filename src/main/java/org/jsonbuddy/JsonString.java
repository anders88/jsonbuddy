package org.jsonbuddy;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.Objects;

public class JsonString extends JsonValue {

    private final String value;

    public JsonString(String value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return value;
    }

    @Override
    public Object javaObjectValue() {
        return value;
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append("\"");
        StringBuilder val = new StringBuilder(value);
        replace(val,'\\','\\');
        replace(val,'\n','n');
        replace(val,'"','"');
        replace(val,'\b','b');
        replace(val,'\f','f');
        replace(val,'\r','r');
        replace(val,'\t','t');
        printWriter.append(val.toString());
        printWriter.append(("\""));
    }

    @Override
    public JsonString deepClone() {
        return this;
    }

    private void replace(StringBuilder val, char c, char rep) {
        for (int i=0;i<val.length();i++) {
            if (val.charAt(i) == c) {
                val.deleteCharAt(i);
                val.insert(i,rep);
                val.insert(i,"\\");
                i++;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonString)) return false;
        JsonString that = (JsonString) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public Instant instantValue() {
        return Instant.parse(value);
    }
}
