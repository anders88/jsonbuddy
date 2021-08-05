package org.jsonbuddy;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

public class JsonBuildTest {

    @Test
    public void shouldCreateValue() {
        JsonValue jsonValue = new JsonString("Darth Vader");
        assertThat(jsonValue.stringValue()).isEqualTo("Darth Vader");

    }

    @Test
    public void shouldCreateObjectWithValue() {
        JsonObject jsonObject = new JsonObject()
                .put("name", new JsonString("Darth Vader"));

        assertThat(jsonObject.stringValue("name")).get().isEqualTo("Darth Vader");
        assertThat(jsonObject.stringValue("xxx")).isEmpty();

    }

    @Test
    public void shouldCreateJsonArray() {
        JsonArray jsonArray = new JsonArray()
                .add("Darth")
                .add("Luke");
        assertThat(jsonArray.strings()).containsExactly("Darth","Luke");
    }

    @Test
    public void shouldThrowExceptionIfRequiredValueIsNotPresent() {
        try {
            JsonFactory.jsonObject().requiredString("cake");
            fail("Expected exception");
        } catch (JsonValueNotPresentException e) {
            assertThat(e.getMessage()).isEqualTo("Required key 'cake' does not exist");
        }
    }

    @Test
    public void shouldHandleTextValue() {
        JsonArray array = new JsonArray().add("one").add("two");
        assertThat(array.strings()).containsExactly("one","two");
    }

    @Test
    public void shouldHandleDates() {
        Instant instant = LocalDateTime.of(2015, 8, 30, 13, 21, 12,314000000).atOffset(ZoneOffset.ofHours(2)).toInstant();
        JsonObject jsonObject = JsonFactory.jsonObject().put("time", instant);

        assertThat(jsonObject.value("time")).isPresent().containsInstanceOf(JsonString.class);
        Optional<String> timetext = jsonObject.stringValue("time");
        assertThat(timetext).isPresent().contains("2015-08-30T11:21:12.314Z");
    }

    @Test
    public void shouldClone() {
        JsonObject orig = JsonFactory.jsonObject()
                .put("name", "Darth Vader")
                .put("properties", JsonFactory.jsonObject().put("religion", "sith"))
                .put("master", "Yoda")
                .put("children", JsonFactory.jsonArray().addAll(Arrays.asList("Luke")));

        JsonObject clone = orig.deepClone();

        assertThat(orig).isEqualTo(clone);

        clone.put("name", "Anakin Skywalker")
                .put("properties", JsonFactory.jsonObject().put("religion", "jedi"))
                .put("children", JsonFactory.jsonArray().addAll(Arrays.asList("Luke", "Leia")));

        assertThat(clone.requiredString("master")).isEqualTo("Yoda");
        assertThat(orig.requiredObject("properties").requiredString("religion")).isEqualTo("sith");
        assertThat(clone.requiredObject("properties").requiredString("religion")).isEqualTo("jedi");
        assertThat(clone.requiredArray("children").stringStream().collect(Collectors.toList())).containsExactly("Luke","Leia");
        assertThat(orig.requiredArray("children").stringStream().collect(Collectors.toList())).containsExactly("Luke");

    }

    @Test
    public void shouldHandleNullAsStringValue() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("nullValue", new JsonNull());

        assertThat(jsonObject.value("nullValue")).isPresent();
        assertThatThrownBy(() -> jsonObject.requiredString("nullValue"))
            .hasMessageContaining("nullValue");
    }

    @Test
    public void shouldGiveStringFormattetAsInstantAsInstant() {
        Instant now = Instant.now();
        JsonObject jsonObject = JsonFactory.jsonObject().put("now", JsonFactory.jsonString(now.toString()));
        assertThat(jsonObject.requiredInstant("now")).isEqualTo(now);


    }
}
