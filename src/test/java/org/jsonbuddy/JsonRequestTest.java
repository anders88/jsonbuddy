package org.jsonbuddy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.assertj.core.api.AbstractThrowableAssert;
import org.jsonbuddy.parse.JsonHttpException;
import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

public class JsonRequestTest {

    @Test
    public void shouldGetJson() throws IOException {
        JsonObject o = JsonParser.parseToObject(new URL("https://httpbin.org/json"));
        assertThat(o.keys()).contains("slideshow");
    }

    @Test
    public void shouldHandleJsonInErrors() throws IOException {
        // This URL used by Google Oauth2 is used for POST and returns 405 when used with GET
        URL url = new URL("https://accounts.google.com/o/oauth2/token");
        URLConnection connection = url.openConnection();

        AbstractThrowableAssert<?,?> exception = assertThatThrownBy(() -> JsonParser.parseToObject(connection))
            .isInstanceOf(JsonHttpException.class)
            .hasMessageContaining("404 Not Found")
            .hasMessageContaining(url.toString());
//        exception
//            .extracting("jsonError")
//            .containsOnly(new JsonObject().put("error", "invalid_request"));
    }

    @Test
    public void shouldHandleTextInErrors() throws IOException {
        // This URL used by Google Oauth2 is used for browser redirects and returns an error page on missing parameters
        URL url = new URL("https://accounts.google.com/o/oauth2/v2/auth");
        URLConnection connection = url.openConnection();

        AbstractThrowableAssert<?,?> exception = assertThatThrownBy(() -> JsonParser.parseToObject(connection))
            .isInstanceOf(JsonHttpException.class)
            .hasMessageContaining("400 Bad Request")
            .hasMessageContaining(url.toString());
        exception
            .extracting("errorContent")
            .anyMatch(s -> s.toString().contains("<p id=\"errorDescription\">Required parameter is missing: response_type</p>"));
    }

}
