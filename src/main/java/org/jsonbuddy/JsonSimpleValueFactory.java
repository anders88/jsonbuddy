package org.jsonbuddy;

public class JsonSimpleValueFactory<T extends JsonSimpleValue> extends JsonFactory {
    private final T value;

    private JsonSimpleValueFactory(T value) {
        this.value = value;
    }


}
