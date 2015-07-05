package org.jsonbuddy;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonArray extends JsonNode {
    private final List<JsonNode> values;


    public JsonArray() {
        values = new ArrayList<>();
    }

    private JsonArray(Stream<JsonNode> nodeStream) {
        values = nodeStream.collect(Collectors.toList());
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


    public JsonArray add(JsonNode jsonNode) {
        values.add(jsonNode);
        return this;
    }

    public JsonArray add(String text) {
        values.add(new JsonTextValue(text));
        return this;
    }

    public JsonArray add(List<String> values) {
        this.values.addAll(values.stream().map(JsonFactory::jsonText).collect(Collectors.toList()));
        return this;
    }

    public static JsonArray fromStream(Stream<JsonNode> nodeStream) {
        return new JsonArray(nodeStream);
    }
}
