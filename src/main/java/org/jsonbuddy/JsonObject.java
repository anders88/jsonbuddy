package org.jsonbuddy;

import org.jsonbuddy.parse.JsonHttpException;
import org.jsonbuddy.parse.JsonParseException;
import org.jsonbuddy.parse.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * JsonObject represents a dictionary of values that can be looked up
 * by string keys. Each value can be a JsonArray, a number, a string,
 * a boolean or another object.
 * This class is made to resemble the java.util.Map interface,
 * with helper methods to work with different types.
 * For example, given: <code>{"foo":"string", "number":123, "really":false]}</code>,
 * <code>requiredString("foo")</code> with return 'string' and
 * <code>requiredLong("number")</code> will return 123.
 * <p>
 * The methods {@link #doubleValue}, {@link #stringValue}, {@link #longValue},
 * {@link #objectValue}, {@link #arrayValue} return an Optional of the
 * specified type. They return an empty Optional if the key is not present or
 * throws JsonConversionException of the value is of the wrong type.
 * <p>
 * The methods {@link #requiredLong}, {@link #requiredString},
 * {@link #requiredLong}, {@link #requiredObject},
 * {@link #requiredArray} return the specified type if the value is
 * present and convertible to the specified type. They throw
 * {@link JsonValueNotPresentException} if the key is not present or
 * {@link JsonConversionException}
 * if the value is on a wrong type.
 */
public class JsonObject extends JsonNode {

    private final Map<String,JsonNode> values;

    /**
     * Creates an empty JsonObject
     */
    public JsonObject() {
        this.values = new LinkedHashMap<>();
    }

    private JsonObject(Map<String,JsonNode> values) {
        this.values = values;
    }

    /**
     * Parse the Reader as a JsonObject
     *
     * @throws JsonParseException if a JSON syntax error was encountered,
     *             or if the JSON was not a JsonObject
     */
    public static JsonObject read(Reader reader) throws IOException {
        JsonNode result = JsonParser.parseNode(reader);
        if (result instanceof JsonObject) {
            return (JsonObject)result;
        }
        if (result == null) {
            throw new JsonParseException("Expected JSON object got null");
        }
        throw new JsonParseException("Expected JSON object got " + result.getClass());
    }

    /**
     * Parse the String as a JsonObject
     *
     * @throws JsonParseException if a JSON syntax error was encountered,
     *             or if the JSON was not a JsonObject
     */
    public static JsonObject parse(String input) {
        try {
            return read(new StringReader(input));
        } catch (IOException e) {
            throw new RuntimeException("Should never happen with StringReader", e);
        }
    }

    /**
     * Parse base64encoded JSON string to JsonObject. Useful for OpenID Connect usage.

     * @throws JsonParseException if a JSON syntax error was encountered
     * @throws IllegalArgumentException if input not base64encoded
     */
    public static JsonObject parseFromBase64encodedString(String base64encodedJson) throws IllegalArgumentException {
        return parse(new String(Base64.getUrlDecoder().decode(base64encodedJson)));
    }

    /**
     * Parse the InputStream as a JsonObject
     *
     * @throws JsonParseException if a JSON syntax error was encountered,
     *             or if the JSON was not a JsonObject
     */
    public static JsonObject read(InputStream inputStream) throws JsonParseException, IOException {
        return read(new InputStreamReader(inputStream));
    }

    /**
     * GET the contents of the url as a JSON object
     *
     * @throws JsonParseException if a JSON syntax error was encountered,
     *             or if the JSON was not a JsonObject
     * @throws IOException if there was a communication error
     */
    public static JsonObject read(URL url) throws IOException {
        return read(url.openConnection());
    }

    /**
     * GET the contents of the URLConnection as a JSON object
     *
     * @throws JsonParseException if a JSON syntax error was encountered,
     *             or if the JSON was not a JsonObject
     * @throws JsonHttpException if the endpoint returned a 4xx error
     * @throws IOException if there was a communication error
     */
    public static JsonObject read(URLConnection connection) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        JsonHttpException.verifyResponseCode(httpConnection);
        try (InputStream input = connection.getInputStream()) {
            return read(input);
        }
    }

    /**
     * Returns the value of the argument key as a String or an empty
     * Optional if the key is not present.
     *
     * @throws JsonConversionException if the value is a JsonArray or JsonObject.
     */
    public Optional<String> stringValue(String key) throws JsonConversionException {
        return get(key, JsonValue.class).map(JsonValue::stringValue);
    }

    /**
     * Returns the value of the argument key as a String.
     *
     * @throws JsonValueNotPresentException if the key is not present or a wrong type
     */
    public String requiredString(String key) throws JsonValueNotPresentException {
        return stringValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    /**
     * Returns the value of the argument key as a double or an empty
     * Optional if the key is not present.
     *
     * @throws JsonConversionException if the value is not convertible to a number
     */
    public Optional<Double> doubleValue(String key) throws JsonConversionException {
        return numberValue(key).map(Number::doubleValue);
    }

    /**
     * Returns the value of the argument key as a double.
     *
     * @throws JsonValueNotPresentException if the key is not present
     * @throws JsonConversionException if the value is not convertible to a number
     */
    public double requiredDouble(String key) throws JsonValueNotPresentException {
        return doubleValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    /**
     * Returns the value of the argument key as a long or an empty
     * Optional if the key is not present.
     *
     * @throws JsonConversionException if the value is not convertible to a number
     */
    public Optional<Long> longValue(String key) throws JsonConversionException {
        return numberValue(key).map(Number::longValue);
    }

    /**
     * Returns the value of the argument key as a long.
     *
     * @throws JsonValueNotPresentException if the key is not present
     * @throws JsonConversionException if the value is not convertible to a number
     */
    public long requiredLong(String key) throws JsonValueNotPresentException {
        return longValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    /**
     * Returns the value of the argument key as a Number or an empty
     * Optional if the key is not present.
     *
     * @throws JsonConversionException if the value is not convertible to a number
     */
    public Optional<Number> numberValue(String key) throws JsonConversionException {
        JsonNode node = values.get(key);
        if (node == null || node instanceof JsonNull) {
            return Optional.empty();
        }
        if (node instanceof JsonNumber) {
            return Optional.of(((JsonNumber)node).javaObjectValue());
        }
        if (node instanceof JsonValue) {
            String stringValue = node.stringValue();
            if (stringValue.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of(Double.parseDouble(stringValue));
            } catch (NumberFormatException e) {
                throw new JsonConversionException(key + " is not numeric");
            }
        } else {
            throw new JsonConversionException(key + " is not numeric");
        }
    }

    /**
     * Returns the value of the argument key as a boolean or an empty
     * Optional if the key is not present.
     *
     * @throws JsonConversionException if the value is not convertible to a boolean
     */
    public Optional<Boolean> booleanValue(String key) throws JsonConversionException {
        JsonNode node = values.get(key);
        if (node == null || node instanceof JsonNull) {
            return Optional.empty();
        }
        if (node instanceof JsonBoolean) {
            return Optional.of(((JsonBoolean)node).booleanValue());
        }
        if (node instanceof JsonValue) {
            return Optional.of(Boolean.parseBoolean(node.stringValue()));
        } else {
            throw new JsonConversionException(key + " is not boolean");
        }
    }

    /**
     * Returns the value of the argument key as a boolean.
     *
     * @throws JsonValueNotPresentException if the key is not present
     * @throws JsonConversionException if the value is not convertible to a boolean
     */
    public boolean requiredBoolean(String key) throws JsonConversionException, JsonValueNotPresentException {
        return booleanValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    /**
     * Returns the value of the argument key as a JsonObject or an empty
     * Optional if the key is not present.
     *
     *  @throws JsonConversionException if the value is not a JsonObject
     */
    public Optional<JsonObject> objectValue(String key) throws JsonConversionException {
        return get(key, JsonObject.class);
    }

    /**
     * Returns the value of the argument key as a JsonObject.
     *
     * @throws JsonValueNotPresentException if the key is not present or not JsonObject
     */
    public JsonObject requiredObject(String key) throws JsonValueNotPresentException{
        return objectValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    /**
     * Returns the value of the argument key as an {@link Instant} or
     * an empty Optional if the key is not present.
     *
     * @throws DateTimeParseException if the text cannot be parsed as an Instant
     */
    public Optional<Instant> instantValue(String key) {
        return stringValue(key).map(Instant::parse);
    }

    /**
     * Returns the value of the argument key as an {@link Instant}.
     *
     * @throws JsonValueNotPresentException if the key is not present
     * @throws DateTimeParseException if the text cannot be parsed as an Instant
     */
    public Instant requiredInstant(String key) {
        return instantValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    /**
     * Returns the value of the argument key as the specified Enum type or
     * an empty Optional if the key is not present.
     *
     * @throws IllegalArgumentException if the specified enum type has
     *         no constant with a matching name
     */
    private <T extends Enum<T>> Optional<T> enumValue(String key, Class<T> enumType) {
        return stringValue(key).map(s -> Enum.valueOf(enumType, s));
    }

    /**
     * Returns the value of the argument key as the specified Enum type.
     *
     * @throws JsonValueNotPresentException if the key is not present
     * @throws IllegalArgumentException if the specified enum type has
     *         no constant with a matching name
     */
    public <T extends Enum<T>> T requiredEnum(String key, Class<T> enumType) {
        return enumValue(key, enumType).orElseThrow(throwKeyNotPresent(key));
    }

    /**
     * Returns the value of the argument key as a JsonArray or an empty
     * Optional if the key is not present.
     *
     *  @throws JsonConversionException if the value is not a JsonArray
     */
    public Optional<JsonArray> arrayValue(String key) throws JsonConversionException {
        return get(key, JsonArray.class);
    }

    /**
     * Returns the value of the argument key as a JsonArray.
     *
     * @throws JsonValueNotPresentException if the key is not present or not JsonArray
     */
    public JsonArray requiredArray(String key) {
        return arrayValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    /**
     * Returns the value of the argument key as or an empty Optional
     * if the key is not present.
     */
    public Optional<JsonNode> value(String key) {
        return Optional.ofNullable(values.get(key));
    }

    /**
     * Returns the value of the argument key as the argument type or an empty Optional
     * if the key is not present.
     *
     * @throws JsonConversionException if the value is not of the specified type
     */
    public <T extends JsonNode> Optional<T> get(String key, Class<T> t) throws JsonConversionException {
        Optional<JsonNode> value = value(key);
        if (value.isPresent() && !t.isAssignableFrom(value.get().getClass())) {
            throw new JsonConversionException("Can't convert " + key + " to " + t);
        }
        return value.map(node -> (T) node);
    }

    private Supplier<JsonValueNotPresentException> throwKeyNotPresent(String key) {
        return () -> new JsonValueNotPresentException(String.format("Required key '%s' does not exist",key));
    }

    /**
     * Writes the JSON text representation of this JsonArray to the writer
     */
    @Override
    public void toJson(PrintWriter printWriter, String currentIntentation, String indentationAmount) {
        printWriter.append("{");
        if (!indentationAmount.isEmpty()) printWriter.append("\n");
        for (Iterator<Entry<String, JsonNode>> iterator = values.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String,JsonNode> entry = iterator.next();
            printWriter.append(currentIntentation);
            printWriter.append(indentationAmount);
            printWriter.append('"');
            printWriter.append(entry.getKey());
            printWriter.append("\":");
            entry.getValue().toJson(printWriter, currentIntentation + indentationAmount, indentationAmount);

            if (iterator.hasNext()) {
                printWriter.append(",");
            }

            if (!indentationAmount.isEmpty()) printWriter.append("\n");
        }
        printWriter.append(currentIntentation);
        printWriter.append("}");
    }

    /**
     * Associates the specified value with the specified key.
     * If the map previously contained a value for the key,
     * the old value is replaced.
     *
     * @return The previous value or null if there was no value
     * @throws IllegalArgumentException if the value cannot be represented as JSON
     */
    public JsonObject put(String key, Object value) {
        values.put(key, JsonFactory.jsonNode(value));
        return this;
    }

    /**
     * Returns all the keys of this JsonObject.
     */
    public Set<String> keys() {
        return values.keySet();
    }

    /**
     * Removes and returns the value specified by the argument key.
     *
     * @return The previous value or Optional.empty is there were no previous value
     */
    public Optional<JsonNode> remove(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(values.remove(key));
    }

    /**
     * Creates a copy of this JsonObject with all the values copied
     */
    @Override
    public JsonObject deepClone() {
        Map<String, JsonNode> cloned = values.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().deepClone()));
        Map<String, JsonNode> newValues = new HashMap<>(cloned);
        return new JsonObject(newValues);
    }

    /**
     * Returns true if the argument is a JsonObject with the same
     * values as this object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonObject)) return false;
        JsonObject that = (JsonObject) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    /**
     * Returns the number of elements in this JsonObject
     */
    public int size() {
        return values.size();
    }

    /**
     * Returns true if this JsonObject contains no values.
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Removes all the values in this JsonObject.
     */
    public void clear() {
        values.clear();
    }

    /**
     * Returns true if this JsonObject has a value for the specified key.
     *
     * @return true if the value is present and not a JSON null value
     */
    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    /**
     * Put all objects from source JsonObject into this
     *
     * @param source The JsonObject to copy from
     * @return this
     */
    public JsonObject putAll(JsonObject source) {
        values.putAll(source.values);
        return this;
    }
}
