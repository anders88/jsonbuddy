package org.jsonbuddy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URL;

import com.sun.net.httpserver.HttpServer;
import org.assertj.core.api.AbstractThrowableAssert;
import org.jsonbuddy.parse.JsonHttpException;
import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

public class JsonRequestTest {

    @Test
    public void shouldGetJson() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.start();
        httpServer.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Content-type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, 0);
            try (PrintWriter printWriter = new PrintWriter(exchange.getResponseBody())) {
                new JsonObject().put("slideshow", true).toJson(printWriter);
            }
            exchange.close();
        });
        JsonObject o = JsonParser.parseToObject(new URL("http://localhost:" + httpServer.getAddress().getPort()));
        assertThat(o.keys()).contains("slideshow");
    }

    @Test
    public void shouldGetJsonArray() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.start();
        httpServer.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Content-type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, 0);
            try (PrintWriter printWriter = new PrintWriter(exchange.getResponseBody())) {
                new JsonArray().add("slideshow").add("corn").toJson(printWriter);
            }
            exchange.close();
        });
        JsonArray a = JsonParser.parseToArray(new URL("http://localhost:" + httpServer.getAddress().getPort()));
        assertThat(a.strings()).contains("slideshow", "corn");
    }


    @Test
    public void shouldHandleJsonInErrors() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.start();
        int serverPort = httpServer.getAddress().getPort();

        httpServer.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Content-type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(400, 0);
            try (PrintWriter printWriter = new PrintWriter(exchange.getResponseBody())) {
                new JsonObject().put("error", "invalid_request").toJson(printWriter);
            }
            exchange.close();
        });

        URL url = new URL("http://localhost:" + serverPort);

        AbstractThrowableAssert<?,?> exception = assertThatThrownBy(() -> JsonParser.parseToObject(url))
            .isInstanceOf(JsonHttpException.class);
        exception
            .extracting("jsonError")
            .containsOnly(new JsonObject().put("error", "invalid_request"));
    }

    @Test
    public void shouldHandleTextInErrors() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.start();
        int serverPort = httpServer.getAddress().getPort();

        URL url = new URL("http://localhost:" + serverPort);

        AbstractThrowableAssert<?,?> exception = assertThatThrownBy(() -> JsonParser.parseToArray(url))
            .isInstanceOf(JsonHttpException.class)
            .hasMessageContaining("404 Not Found")
            .hasMessageContaining(url.toString());
        exception
            .extracting("errorContent")
            .anyMatch(s -> s.toString().contains("<h1>404 Not Found</h1>No context found for request"));
    }

}
