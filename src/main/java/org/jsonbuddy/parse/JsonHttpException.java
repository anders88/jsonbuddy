package org.jsonbuddy.parse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;

import org.jsonbuddy.JsonObject;

/**
 * Signals that the HTTP endpoint returned a 4xx error code for a URL.
 * The exception contains the body of the HTTP response, parsed
 * as JSON if available.
 */
public class JsonHttpException extends RuntimeException {

    private final int responseCode;
    private JsonObject jsonError;
    private String errorContent;

    public JsonHttpException(HttpURLConnection conn) throws IOException {
        super(conn.getResponseCode() + " " + conn.getResponseMessage() + " on " + conn.getRequestMethod() + " " + conn.getURL());
        this.responseCode = conn.getResponseCode();
        if (conn.getContentType() == null || conn.getErrorStream() == null) {
            return;
        }
        String contentType = conn.getContentType().split(";")[0].trim();
        try (InputStream error = conn.getErrorStream()) {
            if (contentType.equals("application/json")) {
                jsonError = JsonObject.read(error);
            } else {
                errorContent = asString(error);
            }
        }
    }

    public static void verifyResponseCode(HttpURLConnection conn) throws IOException {
        if (conn.getResponseCode() >= 400) {
            throw new JsonHttpException(conn);
        }
    }

    public int getResponseCode() {
        return responseCode;
    }

    private String asString(InputStream error) throws IOException {
        try (Reader reader = new InputStreamReader(error)) {
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[1024*1024];
            int c;
            while ((c = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, c);
            }
            return builder.toString();
        }
    }

    public JsonObject getJsonError() {
        return jsonError;
    }

    public String getErrorContent() {
        return errorContent;
    }

}
