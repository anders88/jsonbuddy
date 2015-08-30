package org.jsonbuddy;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class JsonBuildTest {

    @Test
    public void shouldCreateValue() throws Exception {
        JsonSimpleValue jsonSimpleValue = new JsonTextValue("Darth Vader");
        assertThat(jsonSimpleValue.stringValue()).isEqualTo("Darth Vader");

    }

    @Test
    public void shouldCreateObjectWithValue() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .withValue("name", new JsonTextValue("Darth Vader"));

        assertThat(jsonObject.stringValue("name").get()).isEqualTo("Darth Vader");
        assertThat(jsonObject.stringValue("xxx").isPresent()).isFalse();

    }

    @Test
    public void shouldCreateJsonArray() throws Exception {
        JsonArray jsonArray = new JsonArray()
                .add("Darth")
                .add("Luke");
        assertThat(jsonArray.nodeStream()
                .map(an -> ((JsonSimpleValue) an).stringValue())
                .collect(Collectors.toList())).containsExactly("Darth","Luke");
    }

    @Test
    public void shouldThrowExceptionIfRequiredValueIsNotPresent() throws Exception {
        try {
            JsonFactory.jsonObject().requiredString("cake");
            fail("Expected exception");
        } catch (JsonValueNotPresentException e) {
            assertThat(e.getMessage()).isEqualTo("Required key 'cake' does not exsist");
        }
    }

    @Test
    public void shouldHandleTextValue() throws Exception {
        JsonArray array = new JsonArray().add("one").add("two");
        List<String> values = array.nodeStream().map(JsonNode::textValue).collect(Collectors.toList());
        assertThat(values).containsExactly("one","two");
    }

    @Test
    public void shouldHandleDates() throws Exception {
        Instant instant = LocalDateTime.of(2015, 8, 30, 13, 21, 12,314000000).atOffset(ZoneOffset.ofHours(2)).toInstant();
        JsonObject jsonObject = JsonFactory.jsonObject().withInstance("time", instant);

        assertThat(jsonObject.value("time")).isPresent().containsInstanceOf(JsonInstantValue.class);
        Optional<String> timetext = jsonObject.stringValue("time");
        assertThat(timetext).isPresent().contains("2015-08-30T11:21:12.314Z");
    }
}
