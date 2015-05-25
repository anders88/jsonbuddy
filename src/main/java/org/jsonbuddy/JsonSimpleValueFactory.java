package org.jsonbuddy;

public class JsonSimpleValueFactory extends JsonFactory {
    private final JsonSimpleValue value;

    private JsonSimpleValueFactory(JsonSimpleValue value) {
        this.value = value;
    }

    public static JsonSimpleValueFactory text(String value) {
        return new JsonSimpleValueFactory(new JsonTextValue(value));
    }

    public static JsonSimpleValueFactory longNumber(long value) {
        return new JsonSimpleValueFactory(new JsonLong(value));
    }

    public static JsonSimpleValueFactory doubleNumber(double value) {
        return new JsonSimpleValueFactory(new JsonDouble(value,0L));
    }
    public JsonSimpleValue create() {
        return value;
    }

}
