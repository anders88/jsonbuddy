package org.jsonbuddy;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonArray extends JsonNode {
    private final List<JsonNode> values;

    JsonArray(JsonArrayFactory factory) {
        List<JsonNode> collect = factory.nodes.stream().map(JsonFactory::create).collect(Collectors.toList());
        values = Collections.unmodifiableList(collect);
    }

    public Stream<JsonNode> nodeStream() {
        return values.stream();
    }

    @Override
    public void toJson(PrintWriter printWriter) {

    }
}
