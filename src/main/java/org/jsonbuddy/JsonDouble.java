package org.jsonbuddy;

import java.io.PrintWriter;
import java.text.NumberFormat;

public class JsonDouble extends JsonSimpleValue {
    private final double value;

    public JsonDouble(double value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return Double.toString(value);
    }

    @Override
    public Object javaObjectValue() {
        return value;
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append(stringValue());
    }

    public double doubleValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonDouble that = (JsonDouble) o;

        return Double.compare(that.value, value) == 0;

    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return (int) (temp ^ (temp >>> 32));
    }
}
