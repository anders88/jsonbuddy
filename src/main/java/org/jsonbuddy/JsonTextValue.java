package org.jsonbuddy;

import java.io.PrintWriter;

public class JsonTextValue extends JsonSimpleValue {
    private final String value;

    public JsonTextValue(String value) {
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
        if (o == null || getClass() != o.getClass()) return false;

        JsonTextValue that = (JsonTextValue) o;

        return !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
