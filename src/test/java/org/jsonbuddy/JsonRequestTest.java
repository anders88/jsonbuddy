package org.jsonbuddy;

import com.sun.net.httpserver.HttpServer;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jsonbuddy.parse.JsonHttpException;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        JsonObject o = JsonObject.read(new URL("http://localhost:" + httpServer.getAddress().getPort()));
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
        JsonArray a = JsonArray.read(new URL("http://localhost:" + httpServer.getAddress().getPort()));
        assertThat(a.strings()).contains("slideshow", "corn");
    }

    private JsonObject postedObject;

    @Test
    public void shouldPostJson() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.start();
        httpServer.createContext("/", exchange -> {
            postedObject = JsonObject.read(exchange.getRequestBody());
            exchange.getResponseHeaders().add("Content-type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });
        URL url = new URL("http://localhost:" + httpServer.getAddress().getPort());

        JsonObject postingObject = new JsonObject().put("Hello", "World");
        postingObject.postJson(url.openConnection());

        assertThat(postedObject).isEqualTo(postingObject);
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

        assertThatThrownBy(() -> JsonObject.read(url))
            .asInstanceOf(InstanceOfAssertFactories.type(JsonHttpException.class))
            .extracting(JsonHttpException::getJsonError)
            .isEqualTo(new JsonObject().put("error", "invalid_request"));
    }

    @Test
    public void shouldHandleTextInErrors() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.start();
        int serverPort = httpServer.getAddress().getPort();

        URL url = new URL("http://localhost:" + serverPort);
        
        
        assertThatThrownBy(() -> JsonArray.read(url))
            .hasMessageContaining("404 Not Found")
            .hasMessageContaining(url.toString())
            .asInstanceOf(InstanceOfAssertFactories.type(JsonHttpException.class))
            .extracting(JsonHttpException::getErrorContent)
            .asString()
            .contains("<h1>404 Not Found</h1>No context found for request");
    }
    
    @Test
    public void shouldHandleErrorCode() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.start();
        int serverPort = httpServer.getAddress().getPort();

        URL url = new URL("http://localhost:" + serverPort);

        assertThatThrownBy(() -> JsonArray.read(url))
            .asInstanceOf(InstanceOfAssertFactories.type(JsonHttpException.class))
            .extracting(JsonHttpException::getResponseCode)
            .isEqualTo(404);
    }

}
