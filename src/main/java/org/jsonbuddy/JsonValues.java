package org.jsonbuddy;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;

public class JsonValues {

    public static Object deepClone(Object o) {
        if (isJsonValueType(o)) {
            return o;
        } else if (o instanceof JsonArray) {
            return ((JsonArray)o).deepClone();
        } else if (o instanceof JsonObject) {
            return ((JsonObject)o).deepClone();
        } else {
            throw new IllegalArgumentException("Unexpected type " + o.getClass() + " in JsonArray");
        }
    }

    public static Object asJsonValue(Object o) {
        if (o instanceof Instant) {
            return o.toString();
        } else if (o instanceof Enum) {
            return o.toString();
        } else if (o instanceof List) {
            return JsonArray.fromNodeList(((List<?>) o));
        } else if (o instanceof Integer || o instanceof Short || o instanceof Byte) {
            return ((Number)o).longValue();
        } else if (o == null) {
            return new JsonNull();
        } else if (isPureJsonValue(o)) {
            return o;
        } else {
            throw new IllegalArgumentException("Invalid JSON value " + o.getClass());
        }
    }

    public static void toJson(Object node, PrintWriter printWriter) {
         if (node instanceof String) {
            toJson(node.toString(), printWriter);
        } else if (node instanceof JsonArray) {
            ((JsonArray)node).toJson(printWriter);
        } else if (node instanceof JsonObject) {
            ((JsonObject)node).toJson(printWriter);
        } else if (node instanceof JsonNull) {
            printWriter.append(null);
        } else if (node instanceof Number) {
            printWriter.append(node.toString());
        } else if (node instanceof Boolean) {
            printWriter.append(node.toString());
        } else {
            throw new IllegalArgumentException("Unexpected type " + node.getClass() + " in JsonArray");
        }
    }

    private static void toJson(String s, PrintWriter printWriter) {
        StringBuilder val = new StringBuilder(s);
        replace(val,'\\','\\');
        replace(val,'\n','n');
        replace(val,'"','"');
        replace(val,'\b','b');
        replace(val,'\f','f');
        replace(val,'\r','r');
        replace(val,'\t','t');
        printWriter.append("\"");
        printWriter.append(val.toString());
        printWriter.append(("\""));
    }

    /**
     * Returns true if this is a value that can be stored in a JsonArray or JsonObject
     */
    private static boolean isPureJsonValue(Object o) {
        return isJsonValueType(o) || o instanceof JsonArray || o instanceof JsonObject;
    }

    /**
     * Returns true if this is a value that can be stored in a JsonArray or JsonObject
     * and that has value semantics
     */
    private static boolean isJsonValueType(Object o) {
        return o instanceof Boolean || o instanceof String || o instanceof Long || o instanceof Double || o instanceof JsonNull;
    }

    private static void replace(StringBuilder val, char c, char rep) {
        for (int i=0;i<val.length();i++) {
            if (val.charAt(i) == c) {
                val.deleteCharAt(i);
                val.insert(i,rep);
                val.insert(i,"\\");
                i++;
            }
        }
    }
}
