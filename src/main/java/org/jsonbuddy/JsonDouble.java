package org.jsonbuddy;

import java.io.PrintWriter;
import java.text.NumberFormat;

public class JsonDouble extends JsonSimpleValue {
    private final double value;

    JsonDouble(double value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return Double.toString(value);
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append(stringValue());
    }

    public double doubleValue() {
        return value;
    }
}
