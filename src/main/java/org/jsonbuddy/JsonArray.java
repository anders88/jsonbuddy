package org.jsonbuddy;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JsonArray represents an indexed list of values. Each value can be
 * a JsonObject, a number, a string, a boolean or another array.
 * This class is made to resemble the java.util.List interface,
 * with helper methods to work with different types.
 * For example, given: <code>['string', 123, false, {"foo": "bar"}]</code>,
 * <code>requiredString(0)</code> with return 'string' and
 * <code>requiredLong(1)</code> will return 123.
 * <p>
 * If the index is out of bounds for the array or has the wrong data type,
 * JsonValueNotPresentException is thrown.
 * <p>
 * For convenience, <code>objects(Function)</code> will assume all entries
 * are JsonObjects and call the supplied function on them. Similarly,
 * the method <code>strings()</code> will return a List of all the elements as strings.
 */
public class JsonArray extends JsonNode implements Iterable<JsonNode> {

    private final List<JsonNode> values;

    /**
     * Creates an empty JsonArray
     */
    public JsonArray() {
        values = new ArrayList<>();
    }

    private JsonArray(List<? extends JsonNode> nodes) {
        this.values = new ArrayList<>(nodes);
    }

    /**
     * Creates JsonArray with the nodes in the argument list
     */
    public static JsonArray fromNodeList(List<? extends JsonNode> nodes) {
        return new JsonArray(nodes);
    }

    /**
     * Collects the argument stream into a JsonArray
     */
    public static JsonArray fromNodeStream(Stream<? extends JsonNode> nodes) {
        JsonArray jsonNodes = new JsonArray();
        nodes.forEach(jsonNodes::add);
        return jsonNodes;
    }

    /**
     * Creates a JsonArray of Strings
     */
    public static JsonArray fromStrings(String... strings) {
        return fromStringList(Arrays.asList(strings));
    }

    /**
     * Creates a JsonArray of Strings
     */
    public static JsonArray fromStringList(List<String> nodes) {
        if (nodes == null) {
            return new JsonArray();
        }
        return new JsonArray(nodes.stream().map(JsonString::new).collect(Collectors.toList()));
    }

    /**
     * Collects the argument stream into a JsonArray with Strings
     */
    public static JsonArray fromStringStream(Stream<String> nodes) {
        return new JsonArray(nodes.map(JsonString::new).collect(Collectors.toList()));
    }

    /**
     * Maps the values over the function and returns a JsonArray with the results
     */
    public static <T> JsonArray map(Collection<T> values, Function<T, JsonNode> f) {
        return fromNodeStream(values.stream().map(o -> f.apply(o)));
    }

    /**
     * Returns a list of this JsonArray of JsonObjects mapped over the function.
     * Skips values that are not JsonObjects.
     */
    public <T> List<T> objects(Function<JsonObject,T> mapFunc) {
        return nodeStream()
                .filter(n -> n instanceof JsonObject)
                .map(n -> (JsonObject) n)
                .map(mapFunc)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of all the string values of the members this JsonArray that
     * are not JsonObjects or JsonArrays. Skips the JsonObjects and JsonArray
     *
     */
    public List<String> strings() {
        return stringStream().collect(Collectors.toList());
    }

    /**
     * Returns a list of the members of this JsonArray mapped over the function.
     */
    public <T> List<T> mapNodes(Function<JsonNode,T> mapFunc) {
        return nodeStream().map(mapFunc).collect(Collectors.toList());
    }

    /**
     * Returns a stream of the members of this JsonArray.
     */
    public Stream<JsonNode> nodeStream() {
        return values.stream();
    }

    /**
     * Returns a stream of all the string values of the members this JsonArray that
     * are not JsonObjects or JsonArrays. Skips the JsonObjects and JsonArray
     *
     */
    public Stream<String> stringStream() {
        return nodeStream()
                .filter(no -> no instanceof JsonValue)
                .map(no -> ((JsonValue) no).stringValue());
    }

    /**
     * Writes the JSON text representation of this JsonArray to the writer
     */
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

    /**
     * Creates a copy of this JsonArray with all the values copied
     */
    @Override
    public JsonArray deepClone() {
        return new JsonArray(mapNodes(JsonNode::deepClone));
    }

    /**
     * Appends the argument to the end of the JsonArray
     */
    public JsonArray add(Object o) {
        values.add(JsonFactory.jsonNode(o));
        return this;
    }

    /**
     * Appends the arguments to the end of the JsonArray
     */
    public JsonArray addAll(List<String> values) {
        this.values.addAll(values.stream().map(JsonFactory::jsonString).collect(Collectors.toList()));
        return this;
    }

    /**
     * Returns the number of elements in this JsonArray
     */
    public int size() {
        return values.size();
    }

    /**
     * Returns the value at the argument position as a JsonArray.
     *
     * @throws JsonConversionException if the value at the position is not a JsonArray
     */
    public JsonArray requiredArray(int pos) throws JsonConversionException {
        return get(pos, JsonArray.class);
    }

    /**
     * Returns the value at the argument position as a JsonObject.
     *
     * @throws JsonConversionException if the value at the position is not a JsonObject
     */
    public JsonObject requiredObject(int pos) throws JsonConversionException {
        return get(pos, JsonObject.class);
    }

    /**
     * Returns the value at the argument position as a String.
     *
     * @throws JsonConversionException if the value at the position is not a String
     */
    public CharSequence requiredString(int pos) throws JsonConversionException {
        return get(pos, JsonValue.class).stringValue();
    }


    /**
     * Returns the value at the argument position as a long.
     *
     * @throws JsonConversionException if the value at the position is not numeric
     */
    public long requiredLong(int position) throws JsonConversionException {
        return requiredNumber(position).longValue();
    }

    /**
     * Returns the value at the argument position as a double.
     *
     * @throws JsonConversionException if the value at the position is not numeric
     */
    public double requiredDouble(int position) throws JsonConversionException {
        return requiredNumber(position).doubleValue();
    }

    /**
     * Returns the value at the argument position as a boolean.
     *
     * @throws JsonConversionException if the value at the position is not a boolean
     */
    public boolean requiredBoolean(int position) throws JsonConversionException {
        if (get(position) instanceof JsonBoolean) {
            return ((JsonBoolean)get(position)).booleanValue();
        } else if (get(position) instanceof JsonValue) {
            return Boolean.parseBoolean(((JsonValue)get(position)).stringValue());
        } else {
            throw new JsonConversionException(position + " is not boolean");
        }
    }

    /**
     * Returns the value at the argument position as a number.
     *
     * @throws JsonConversionException if the value at the position is not numeric
     */
    private Number requiredNumber(int position) throws JsonConversionException {
        if (get(position) instanceof JsonNumber) {
            return ((JsonNumber)get(position)).javaObjectValue();
        } else if (get(position) instanceof JsonValue) {
            try {
                return Double.parseDouble(((JsonValue)get(position)).stringValue());
            } catch (NumberFormatException e) {
                throw new JsonConversionException(position + " is not numeric");
            }
        } else {
            throw new JsonConversionException(position + " is not numeric");
        }
    }

    /**
     * Returns the value at the argument position converted to the argument class.
     *
     * @throws JsonValueNotPresentException if the array does not have a value at the argument position
     * @throws JsonConversionException if the value at the position is not the correct class
     */
    public <T> T get(int pos, Class<T> jsonClass) throws JsonConversionException, JsonValueNotPresentException {
        JsonNode jsonNode = get(pos);
        if (!jsonClass.isAssignableFrom(jsonNode.getClass())) {
            throw new JsonConversionException(String.format("Object in array (%s) is not %s",jsonNode.getClass().getName(),jsonClass.getName()));
        }
        return (T) jsonNode;
    }

    private JsonNode get(int pos) throws JsonValueNotPresentException {
        if (pos < 0 || pos >= size()) {
            throw new JsonValueNotPresentException("Json array does not have a value at position " + pos);
        }
        return values.get(pos);
    }

    /**
     * Returns true if the argument is a JsonArray with the same
     * values as this object
     */
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

    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Removes the value at the specified position. Returns the value that was removed.
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public JsonNode remove(int i) {
        return values.remove(i);
    }

    /**
     * Removes all values in this JsonArray
     */
    public void clear() {
        values.clear();
    }

    /**
     * Replaces the value at the specified position.
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public void set(int i, Object o) {
        values.set(i, JsonFactory.jsonNode(o));
    }

    /**
     * Returns a view of the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive
     *
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *         (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
     *         fromIndex &gt; toIndex</tt>)
     */
    public JsonArray subList(int fromIndex, int toIndex) {
        return new JsonArray(values.subList(fromIndex, toIndex));
    }
}
