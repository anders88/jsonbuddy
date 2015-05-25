package org.jsonbuddy;

public class JsonSimpleValueFactory<T extends JsonSimpleValue> extends JsonFactory {
    private final T value;

    private JsonSimpleValueFactory(T value) {
        this.value = value;
    }

    public static JsonSimpleValueFactory<JsonTextValue> text(String value) {
        return new JsonSimpleValueFactory<>(new JsonTextValue(value));
    }

    public static JsonSimpleValueFactory<JsonLong> longNumber(long value) {
        return new JsonSimpleValueFactory<>(new JsonLong(value));
    }

    public static JsonSimpleValueFactory<JsonDouble> doubleNumber(double value) {
        return new JsonSimpleValueFactory<>(new JsonDouble(value));
    }

    public static JsonSimpleValueFactory<JsonBooleanValue> trueValue() {
        return new JsonSimpleValueFactory<>(new JsonBooleanValue(true));
    }

    public static JsonSimpleValueFactory<JsonBooleanValue> falseValue() {
        return new JsonSimpleValueFactory<>(new JsonBooleanValue(false));
    }

    public static JsonSimpleValueFactory<JsonNullValue> nullValue() {
        return new JsonSimpleValueFactory<>(new JsonNullValue());
    }


    public T create() {
        return value;
    }


}
