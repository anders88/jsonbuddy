package org.jsonbuddy;

import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonArray extends JsonNode implements Iterable<JsonNode> {

    private final List<JsonNode> values;

    public JsonArray() {
        values = new ArrayList<>();
    }

    private JsonArray(List<? extends JsonNode> nodes) {
        this.values = new ArrayList<>(nodes);
    }

    public static JsonArray fromNodeList(List<? extends JsonNode> nodes) {
        return new JsonArray(nodes);
    }

    public static JsonArray fromNodeStream(Stream<? extends JsonNode> nodes) {
        return new JsonArray(nodes.collect(Collectors.toList()));
    }

    public static JsonArray fromStrings(String... strings) {
        return fromStringList(Arrays.asList(strings));
    }

    public static JsonArray fromStringList(List<String> nodes) {
        if (nodes == null) {
            return new JsonArray();
        }
        return new JsonArray(nodes.stream().map(JsonString::new).collect(Collectors.toList()));
    }

    public static JsonArray fromStringStream(Stream<String> nodes) {
        return new JsonArray(nodes.map(JsonString::new).collect(Collectors.toList()));
    }

    public static <T> JsonArray map(Collection<T> values, Function<T, JsonNode> f) {
        return fromNodeStream(values.stream().map(o -> f.apply(o)));
    }

    public <T> List<T> objects(Function<JsonObject,T> mapFunc) {
        return nodeStream()
                .filter(jn -> (jn instanceof JsonObject))
                .map(jn -> (JsonObject) jn)
                .map(mapFunc)
                .collect(Collectors.toList());
    }

    public List<String> strings() {
        return stringStream().collect(Collectors.toList());
    }

    public <T> List<T> mapNodes(Function<JsonNode,T> mapFunc) {
        return nodeStream().map(mapFunc).collect(Collectors.toList());
    }

    public Stream<JsonNode> nodeStream() {
        return values.stream();
    }

    public Stream<String> stringStream() {
        return nodeStream()
                .filter(no -> no instanceof JsonValue)
                .map(no -> ((JsonValue) no).stringValue());
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
        values.add(new JsonString(text));
        return this;
    }

    public JsonArray addAll(List<String> values) {
        this.values.addAll(values.stream().map(JsonFactory::jsonText).collect(Collectors.toList()));
        return this;
    }

    public int size() {
        return values.size();
    }

    public <T> T get(int pos,Class<T> jsonClass) {
        if (pos < 0 || pos >= size()) {
            throw new JsonValueNotPresentException("Json array does not have a value at position " + pos);
        }
        JsonNode jsonNode = values.get(pos);
        if (!jsonClass.isAssignableFrom(jsonNode.getClass())) {
            throw new JsonValueNotPresentException(String.format("Object in array (%s) is not %s",jsonNode.getClass().getName(),jsonClass.getName()));

        }
        return (T) jsonNode;
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
