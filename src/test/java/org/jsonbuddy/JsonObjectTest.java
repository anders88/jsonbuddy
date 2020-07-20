package org.jsonbuddy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;

import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

public class JsonObjectTest {

    @Test
    public void shouldGiveStringAsDouble() {
        JsonObject obj = JsonFactory.jsonObject()
                .put("pi", "3.14")
                .put("null", new JsonNull());
        assertThat(obj.requiredDouble("pi")).isEqualTo(3.14d);
        assertThat(obj.requiredLong("pi")).isEqualTo(3);
        assertThat(obj.longValue("missing")).isEmpty();
        assertThat(obj.doubleValue("missing")).isEmpty();
        assertThat(obj.doubleValue("null")).isEmpty();
        assertThat(obj.isArray()).isFalse();
        assertThat(obj.isObject()).isTrue();
    }

    @Test
    public void shouldHandleEmptyNumericStrings() {
        assertThat(new JsonObject().put("value", "").longValue("value")).isEmpty();
    }

    @Test
    public void instantValues() {
        Instant instant = Instant.now();
        JsonObject o = new JsonObject().put("instant", instant);
        assertThat(o.requiredInstant("instant")).isEqualTo(instant);
        assertThat(o.instantValue("missing")).isEmpty();
    }

    @Test
    public void requiredDoubleThrowsOnNonNumeric() {
        assertThatThrownBy(() -> new JsonObject().put("nan", "test").requiredDouble("nan"))
            .hasMessageContaining("nan is not numeric");
        assertThatThrownBy(() -> new JsonObject().put("obj", new JsonObject()).requiredDouble("obj"))
            .hasMessageContaining("obj is not numeric");
    }

    @Test
    public void handleInvalidTypes() {
        assertThatThrownBy(() -> new JsonObject().put("a", new Object()))
            .hasMessageContaining("Invalid JsonNode class java.lang.Object");
    }

    @Test
    public void shouldRemove() {
        JsonObject o = new JsonObject().put("key", "value");
        assertThat(o.remove("key")).contains(new JsonString("value"));
        assertThat(o.stringValue("key")).isEmpty();
        assertThat(o.keys()).isEmpty();
        assertThat(o.size()).isZero();
        assertThat(o.isEmpty()).isTrue();

        assertThat(o.remove(null)).isEmpty();
    }

    @Test
    public void shouldDealSensiblyWithNulls() {
        JsonObject o = new JsonObject()
                .put("jsonNull", new JsonNull())
                .put("objectNull", null);
        assertThat(o.containsKey("jsonNull")).isTrue();
        assertThat(o.keys()).contains("jsonNull", "objectNull");
    }

    @Test
    public void shouldHandleEnumValues() {
        JsonObject o = new JsonObject().put("state", Thread.State.WAITING);
        assertThat(o.requiredString("state")).isEqualTo("WAITING");
        assertThat(o.requiredEnum("state", Thread.State.class))
            .isEqualTo(Thread.State.WAITING);
    }

    @Test
    public void shouldHandleBigDecimalValues() {
        StringBuilder numberAsString = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            numberAsString.append("910");
        }
        numberAsString.append(".125");

        BigDecimal largeNumber = new BigDecimal(numberAsString.toString());

        JsonObject o = new JsonObject().put("largeNumber", largeNumber);
        o = JsonObject.parse(o.toJson());
        assertThat(o.numberValue("largeNumber")).contains(largeNumber);
        assertThat(o.requiredString("largeNumber")).isEqualTo(numberAsString.toString());
    }

    @Test
    public void shouldClearObject() {
        JsonObject o = new JsonObject().put("string", "value");
        o.clear();
        assertThat(o.isEmpty()).isTrue();
    }


    @Test
    public void shouldSupportAllValueTypes() {
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

        assertThat(object.hashCode()).isEqualTo(object.deepClone().hashCode());
        assertThat(object)
            .isEqualTo(object)
            .isEqualTo(object.deepClone())
            .isEqualTo(JsonObject.parse(object.toString()))
            .isNotEqualTo(object.toJson());

        assertThat(object.keys())
            .containsOnly("bool", "double", "enum", "instant", "node", "array", "long", "string", "null");
    }

    @Test
    public void shouldGiveValuesAsString() {
        assertThat(new JsonObject().put("pi", "3.14").stringValue("pi")).get()
            .isEqualTo("3.14");
        assertThat(new JsonObject().put("pi", "3.14").requiredString("pi"))
            .isEqualTo("3.14");
        assertThat(new JsonObject().put("number", 42.5).stringValue("number")).get()
            .isEqualTo("42.5");
        assertThat(new JsonObject().put("number", 42.5).requiredString("number"))
            .isEqualTo("42.5");

        assertThat(JsonFactory.jsonObject().put("anumber", 42).stringValue("anumber"))
            .isPresent().contains("42");
    }

    @Test
    public void shouldHandleNullStrings() {
        assertThat(new JsonObject().put("nullValue", null).stringValue("nullValue")).isEmpty();
        assertThatThrownBy(() -> new JsonObject().put("nullValue", null).requiredString("nullValue"))
            .isInstanceOf(JsonValueNotPresentException.class)
            .hasMessageContaining("nullValue");

        assertThat(new JsonObject().put("nullValue", new JsonNull()).stringValue("nullValue")).isEmpty();
        assertThatThrownBy(() -> new JsonObject().put("nullValue", new JsonNull()).requiredString("nullValue"))
            .isInstanceOf(JsonValueNotPresentException.class)
            .hasMessageContaining("nullValue");
    }

    @Test
    public void booleanShouldReturnAsString() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("abool", true);
        assertThat(jsonObject.stringValue("abool")).isPresent().contains("true");

        assertThatThrownBy(() -> new JsonObject().put("a", new JsonArray()).stringValue("a"))
            .isInstanceOf(JsonConversionException.class);

        assertThat(new JsonObject().stringValue("noSuchKey")).isEmpty();
        assertThatThrownBy(() -> new JsonObject().requiredString("noSuchKey"))
            .isInstanceOf(JsonValueNotPresentException.class)
            .hasMessageContaining("noSuchKey");
    }

    @Test
    public void shouldThrowOnTypeMismatch() {
        assertThatThrownBy(() -> new JsonObject().put("a", "a").objectValue("a"))
            .isInstanceOf(JsonConversionException.class);
    }

    @Test
    public void shouldReturnBoolean() {
        JsonObject o = new JsonObject()
                .put("boolean", true)
                .put("string", "TRuE")
                .put("nonsense", "maybe")
                .put("object", new JsonObject())
                .put("null", null)
                .put("number", 0);
        assertThat(o.requiredBoolean("boolean")).isTrue();
        assertThat(o.booleanValue("string")).isPresent().contains(true);
        assertThat(o.booleanValue("nonsense")).isPresent().contains(false);
        assertThat(o.requiredBoolean("number")).isFalse();

        assertThat(o.booleanValue("null")).isEmpty();
        assertThat(o.booleanValue("missing")).isEmpty();

        assertThatThrownBy(() -> o.booleanValue("object"))
            .hasMessageContaining("not boolean");
    }

    @Test
    public void shouldPutAllProperties() {
        JsonObject source = new JsonObject()
            .put("firstName", "Darth")
            .put("lastName", "Vader")
            .put("forcePowers", new JsonArray()
                    .add("shock").add("lightning"));

        JsonObject target = new JsonObject();
        target.putAll(source);

        assertThat(target.toJson())
            .isEqualTo(source.toJson());
    }

    @Test
    public void shouldConvertFromBase64() {
        JsonObject source = new JsonObject()
                .put("firstName", "Darth")
                .put("lastName", "Vader")
                .put("forcePowers", new JsonArray()
                        .add("shock").add("lightning"));
        String base64EncodedString = Base64.getEncoder().encodeToString(source.toJson().getBytes());
        assertThat(JsonObject.parseFromBase64encodedString(base64EncodedString))
                .isEqualTo(source);
    }

}
