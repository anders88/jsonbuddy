package org.jsonbuddy;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonArray extends JsonNode {
    private final List<JsonNode> values = new ArrayList<>();


    public JsonArray() {

    }

    public Stream<JsonNode> nodeStream() {
        return values.stream();
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append("[");
        boolean notFirst = false;
        for (JsonNode node : values) {
            if (notFirst) {
                printWriter.append(",");
            }
            notFirst = true;
            node.toJson(printWriter);
        }
        printWriter.append("]");
    }

    public JsonArray add(JsonTextValue jsonTextValue) {
        values.add(jsonTextValue);
        return this;
    }

    public JsonArray add(JsonNode jsonNode) {
        values.add(jsonNode);
        return this;
    }
}
