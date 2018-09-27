package org.jsonbuddy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

public class JsonArrayTest {
    @Test
    public void shouldMapValues() throws Exception {
        JsonArray jsonArray = JsonArray.fromNodeList(Arrays.asList(
                JsonFactory.jsonObject().put("name", "Darth"),
                JsonFactory.jsonObject().put("name", "Luke"),
                JsonFactory.jsonObject().put("name", "Leia")
        ));
        List<String> names = jsonArray.objects(jo -> jo.requiredString("name"));
        assertThat(names).containsExactly("Darth","Luke","Leia");
    }

    @Test
    public void shouldContainDifferentValueTypes() throws Exception {
        JsonArray a = new JsonArray()
                .add("test").add(new JsonObject()).add(new JsonArray())
                .add(Instant.now()).add(null).add(Thread.State.WAITING)
                .add(123).add(false).add(3.14);

        assertThat(a)
            .isEqualTo(a).isEqualTo(a.deepClone())
            .isEqualTo(JsonParser.parseToArray(a.toString()))
            .isNotEqualTo(a.toString());
        assertThat(a.isEmpty()).isFalse();
        assertThat(a.size()).isEqualTo(9);
    }

    @Test
    public void shouldReturnValuesAsString() throws Exception {
        JsonArray a = new JsonArray()
                .add("test")
                .add(null)
                .add(Thread.State.WAITING)
                .add(123)
                .add(false)
                .add(3.14);
        assertThat(a.requiredString(0)).isEqualTo("test");
        assertThat(a.requiredString(1)).isNull();
        assertThat(a.requiredString(2)).isEqualTo("WAITING");
        assertThat(a.requiredString(3)).isEqualTo("123");
        assertThat(a.requiredString(4)).isEqualTo("false");
        assertThat(a.requiredString(5)).isEqualTo("3.14");
    }

    @Test
    public void shouldHandleComplexValues() {
        JsonArray a = new JsonArray()
                .add(new JsonObject())
                .add(new JsonArray())
                .add(new JsonNull());
        assertThat(a.requiredObject(0)).isEqualTo(new JsonObject());
        assertThat(a.requiredArray(1)).isEqualTo(new JsonArray());
    }

    @Test
    public void shouldHandleArraysOfArrays() {
        JsonArray subArray1 = new JsonArray().add(1).add(2).add(3);
        JsonArray subArray2 = new JsonArray().add("a").add("b").add("c");
        JsonArray subArray3 = new JsonArray().add(new JsonObject().put("name", "James"));

        JsonArray array = new JsonArray().add(subArray1).add(subArray2).add(subArray3);
        assertThat(array.requiredArray(1)).isEqualTo(subArray2);

        assertThat(array.arrays()).isEqualTo(Arrays.asList(subArray1, subArray2, subArray3));
        assertThat(array.arrayStream())
            .isEqualTo(Arrays.asList(subArray1, subArray2, subArray3));
    }

    @Test
    public void shouldReturnValuesAsLong() throws Exception {
        JsonArray a = new JsonArray().add(123.4).add("3.14").add(42);
        assertThat(a.requiredLong(0)).isEqualTo(123);
        assertThat(a.requiredLong(1)).isEqualTo(3);
        assertThat(a.requiredLong(2)).isEqualTo(42);
        assertThat(a.longs()).isEqualTo(Arrays.asList(123L, 3L, 42L));
    }

    @Test
    public void shouldReturnValuesAsDouble() throws Exception {
        JsonArray a = new JsonArray().add(1234.5).add("3.25").add(42);
        assertThat(a.requiredDouble(0)).isEqualTo(1234.5);
        assertThat(a.requiredDouble(1)).isEqualTo(3.25);
        assertThat(a.requiredDouble(2)).isEqualTo(42);
        assertThat(a.doubles()).isEqualTo(Arrays.asList(1234.5, 3.25, 42.0));
    }

    @Test
    public void shouldReturnValuesAsBoolean() throws Exception {
        JsonArray a = new JsonArray().add(1).add("TrUE").add(false)
                .add(new JsonArray());
        assertThat(a.requiredBoolean(0)).isEqualTo(false);
        assertThat(a.requiredBoolean(1)).isEqualTo(true);
        assertThat(a.requiredBoolean(2)).isEqualTo(false);

        assertThatThrownBy(() -> a.requiredBoolean(3))
            .hasMessageContaining("[] is not boolean");
    }

    @Test
    public void shouldReturnValuesAsBooleanList() throws Exception {
        JsonArray a = new JsonArray().add(1).add("TrUE").add(false);
        assertThat(a.booleans()).isEqualTo(Arrays.asList(false, true, false));
    }

    @Test
    public void shouldHandleInvalidNumbers() throws Exception {
        JsonArray a = new JsonArray().add(new JsonObject()).add("not a number");

        assertThatThrownBy(() -> a.requiredDouble(0))
            .hasMessageContaining("not numeric");
        assertThatThrownBy(() -> a.requiredDouble(1))
            .hasMessageContaining("not numeric");
    }

    @Test
    public void shouldHandleEnumValues() throws Exception {
        JsonArray a = new JsonArray().add(Thread.State.BLOCKED);
        assertThat(a.requiredString(0)).isEqualTo("BLOCKED");
        assertThat(a.requiredEnum(0, Thread.State.class))
            .isEqualTo(Thread.State.BLOCKED);
    }

    @Test
    public void shouldHandleInstantValues() throws Exception {
        Instant time = Instant.ofEpochMilli(1447278780000L);
        JsonArray a = new JsonArray().add(time);
        assertThat(a.requiredString(0)).startsWith("2015-11-11T21:53");
        assertThat(a.requiredInstant(0)).isEqualTo(time);
    }

    @Test
    public void shouldHandleBigDecimalValues() throws Exception {
        StringBuilder numberAsString = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            numberAsString.append("910");
        }
        numberAsString.append(".125");

        BigDecimal largeNumber = new BigDecimal(numberAsString.toString());

        JsonArray o = new JsonArray().add(largeNumber);
        o = JsonParser.parseToArray(o.toJson());
        assertThat(o.requiredNumber(0)).isEqualTo(largeNumber);
        assertThat(o.requiredString(0)).isEqualTo(numberAsString.toString());
    }

    @Test
    public void shouldCreateFromStrings() throws Exception {
        JsonArray jsonArray = JsonArray.fromStringList(Arrays.asList("a", "b", "c"));

        assertThat(jsonArray.get(0, JsonString.class).stringValue()).isEqualTo("a");
        assertThat(jsonArray.get(1, JsonValue.class).stringValue()).isEqualTo("b");
    }

    @Test
    public void hasNoTextValue() throws Exception {
        assertThatThrownBy(() -> new JsonArray().stringValue())
            .hasMessageContaining("Not supported");
    }

    @Test
    public void shouldCreateFromStream() throws Exception {
        assertThat(JsonArray.fromStringStream(Arrays.asList("a", "b", "c").stream()))
            .isEqualTo(JsonArray.fromStringList(Arrays.asList("a", "b", "c")))
            .isEqualTo(JsonArray.fromStrings("a", "b", "c"));
    }

    @Test
    public void shouldSupportNullList() throws Exception {
        assertThat(JsonArray.fromStringList(null)).isEmpty();
    }

    @Test
    public void shouldMapNodes() throws Exception {
        JsonArray array = new JsonArray()
                .add(new JsonObject().put("a", "foo"))
                .add(new JsonObject().put("a", "foobar"));
        assertThat(array.objects(n -> n.requiredString("a").length()))
            .containsExactly(3, 6);
    }

    @Test
    public void shouldThrowOnMissingValues() throws Exception {
        assertThatThrownBy(() -> new JsonArray().get(43, JsonString.class))
            .hasMessageContaining("does not have a value at position 43");
    }

    @Test
    public void shouldThrowOnWrongType() throws Exception {
        assertThatThrownBy(() -> new JsonArray().add("test").get(0, JsonObject.class))
            .hasMessageContaining("String")
            .hasMessageContaining("is not org.jsonbuddy.JsonObject");
    }

    @Test
    public void emptyArrays() throws Exception {
        JsonArray a = new JsonArray();
        assertThat(a.size()).isZero();
        assertThat(a.isEmpty()).isTrue();
    }

    @Test
    public void shouldChangeValues() throws Exception {
        JsonArray a = new JsonArray().add("0").add("1").add("2").add("3");
        a.set(1, "100");
        assertThat(a.strings()).containsExactly("0", "100", "2", "3");
        assertThat(a.subList(1, 3).strings()).containsExactly("100", "2");
        a.set(1, new JsonObject());
        assertThat(a.requiredObject(1)).isEqualTo(new JsonObject());
    }

    @Test
    public void shouldRemoveValues() throws Exception {
        JsonArray a = new JsonArray().add("0").add("1").add("2");
        a.remove(1);
        assertThat(a.strings()).containsExactly("0", "2");
        a.clear();
        assertThat(a.isEmpty()).isTrue();
    }

    @Test
    public void shouldSkipElementsThatAreNotObjectsWhenMappingObjects() throws Exception {
        JsonArray a = new JsonArray()
                .add("0")
                .add(new JsonObject().put("number","one"))
                .add(new JsonObject().put("number","two"));

        assertThat(a.objects(n -> n.requiredString("number"))).containsExactly("one","two");

    }
}
