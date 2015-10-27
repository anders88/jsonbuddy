package org.jsonbuddy;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThatThrownBy;
import java.time.Instant;
import java.util.Arrays;

import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

public class JsonObjectTest {

    @Test
    public void shouldGiveStringAsDouble() throws Exception {
        JsonObject obj = JsonFactory.jsonObject().put("pi", "3.14");
        assertThat(obj.requiredDouble("pi")).isEqualTo(3.14d);
        assertThat(obj.requiredLong("pi")).isEqualTo(3);
    }

    @Test
    public void instantValues() throws Exception {
        Instant instant = Instant.now();
        assertThat(new JsonObject().put("instant", instant).requiredInstant("instant"))
            .isEqualTo(instant);
    }

    @Test
    public void requiredDoubleThrowsOnNonNumeric() throws Exception {
        assertThatThrownBy(() -> new JsonObject().put("nan", "test").requiredDouble("nan"))
            .hasMessageContaining("nan is not numeric");
        assertThatThrownBy(() -> new JsonObject().put("obj", new JsonObject()).requiredDouble("obj"))
            .hasMessageContaining("obj is not numeric");
    }

    @Test
    public void shouldRemove() throws Exception {
        JsonObject o = new JsonObject().put("string", "value");
        o.remove("string");
        assertThat(o.stringValue("string")).isEmpty();
    }


    @Test
    public void shouldSupportAllValueTypes() throws Exception {
        Instant instant = Instant.now();
        JsonObject object = new JsonObject()
                .put("bool", true)
                .put("double", 0.0)
                .put("enum", Thread.State.WAITING)
                .put("instant", instant)
                .put("node", new JsonArray())
                .put("array", Arrays.asList("a", "b"))
                .put("long", 123)
                .put("string", "string")
                .put("null", new JsonNull());
        JsonObject copy = new JsonObject()
                .put("bool", true)
                .put("double", 0.0)
                .put("enum", Thread.State.WAITING)
                .put("instant", instant)
                .put("node", new JsonArray())
                .put("array", Arrays.asList("a", "b"))
                .put("long", 123)
                .put("string", "string")
                .put("null", new JsonNull());

        assertThat(object).isEqualTo(copy);
        assertThat(object.hashCode()).isEqualTo(copy.hashCode());
        assertThat(object.deepClone()).isEqualTo(copy);
        assertThat(JsonParser.parseToObject(object.toJson())).isEqualTo(object);
    }

    @Test
    public void missingNumeric() throws Exception {
        assertThat(new JsonObject().doubleValue("missing")).isEmpty();
    }

    @Test
    public void shouldGiveValuesAsString() throws Exception {
        assertThat(new JsonObject().put("pi", "3.14").stringValue("pi").get())
            .isEqualTo("3.14");
        assertThat(new JsonObject().put("pi", "3.14").requiredString("pi"))
            .isEqualTo("3.14");
        assertThat(new JsonObject().put("number", 42.5).stringValue("number").get())
            .isEqualTo("42.5");
        assertThat(new JsonObject().put("number", 42.5).requiredString("number"))
            .isEqualTo("42.5");

        assertThat(new JsonObject().put("nullValue", (String)null).stringValue("nullValue")).isEmpty();
        assertThatThrownBy(() -> new JsonObject().put("nullValue", (String)null).requiredString("nullValue"))
            .isInstanceOf(JsonValueNotPresentException.class)
            .hasMessageContaining("nullValue");

        assertThat(new JsonObject().put("nullValue", new JsonNull()).stringValue("nullValue")).isEmpty();
        assertThatThrownBy(() -> new JsonObject().put("nullValue", new JsonNull()).requiredString("nullValue"))
            .isInstanceOf(JsonValueNotPresentException.class)
            .hasMessageContaining("nullValue");
    }

    @Test
    public void shouldReturnANumberAsString() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("anumber", 42);
        assertThat(jsonObject.stringValue("anumber")).isPresent().contains("42");
    }

    @Test
    public void booleanShouldReturnAsString() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("abool", true);
        assertThat(jsonObject.stringValue("abool")).isPresent().contains("true");

        assertThat(new JsonObject().stringValue("noSuchKey")).isEmpty();
        assertThatThrownBy(() -> new JsonObject().requiredString("noSuchKey"))
            .isInstanceOf(JsonValueNotPresentException.class)
            .hasMessageContaining("noSuchKey");
    }
}
