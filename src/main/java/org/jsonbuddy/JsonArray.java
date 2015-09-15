package org.jsonbuddy;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonArray extends JsonNode implements Iterable<JsonNode> {
    private final List<JsonNode> values;


    public JsonArray() {
        values = new ArrayList<>();
    }


    private JsonArray(List<? extends JsonNode> nodes) {
        List<JsonNode> myVals = new ArrayList<>();
        myVals.addAll(nodes);
        this.values = myVals;
    }

    public static JsonArray fromNodeList(List<? extends JsonNode> nodes) {
        return new JsonArray(nodes);
    }

    public static JsonArray fromNodeSteam(Stream<? extends JsonNode> nodes) {
        return new JsonArray(nodes.collect(Collectors.toList()));
    }


    public static JsonArray fromStringList(List<String> nodes) {
        if (nodes == null) {
            return new JsonArray();
        }
        return new JsonArray(nodes.stream().map(JsonTextValue::new).collect(Collectors.toList()));
    }

    public static JsonArray fromStringStream(Stream<String> nodes) {
        return new JsonArray(nodes.map(JsonTextValue::new).collect(Collectors.toList()));
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

    @Override
    public JsonArray deepClone() {
        return new JsonArray(
                values.stream()
                .map(JsonNode::deepClone)
                .collect(Collectors.toList()));
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

    public int size() {
        return values.size();
    }

    public <T> T get(int pos,Class<T> jsonClass) {
        if (pos < 0 || pos >= size()) {
            throw new JsonValueNotPresentException("Json array does not have a value at position " + pos);
        }
        JsonNode jsonNode = values.get(pos);
        if (!jsonNode.getClass().isAssignableFrom(jsonClass)) {
            throw new JsonValueNotPresentException(String.format("Object in array (%s) is not %s",jsonNode.getClass().getName(),jsonClass.getName()));

        }
        return (T) jsonNode;
    }

    public Stream<String> stringStream() {
        return nodeStream()
                .filter(no -> no instanceof JsonSimpleValue)
                .map(no -> ((JsonSimpleValue) no).stringValue())
                ;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonArray)) return false;
        JsonArray jsonArray = (JsonArray) o;
        return Objects.equals(values, jsonArray.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public Iterator<JsonNode> iterator() {
        return new ArrayList<>(values).iterator();
    }
}
