package org.jsonbuddy;

import java.io.PrintWriter;
import java.text.NumberFormat;

public class JsonDouble extends JsonSimpleValue {
    private final double value;
    private final long exp;

    JsonDouble(double value, long exp) {
        this.value = value;
        this.exp = exp;
    }

    @Override
    public String stringValue() {
        return Double.toString(Math.pow(value,exp));
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append(NumberFormat.getInstance().format(value));
        if (exp != 1L) {
            printWriter.append("e");
            printWriter.append(Long.toString(exp));
        }
    }
}
