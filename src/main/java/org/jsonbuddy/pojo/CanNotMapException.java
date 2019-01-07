package org.jsonbuddy.pojo;

public class CanNotMapException extends JsonException {
    public CanNotMapException(String message) {
        super(message);
    }

    public CanNotMapException(Throwable cause) {
        super(cause);
    }
}
