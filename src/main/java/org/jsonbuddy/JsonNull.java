package org.jsonbuddy;


public class JsonNull  {
    public JsonNull() {
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JsonNull);
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
