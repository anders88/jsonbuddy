package org.jsonbuddy;

public class JsonValueNotPresentException extends RuntimeException {
    public JsonValueNotPresentException(String message) {
        super(message);
    }
}
