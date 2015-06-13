package org.jsonbuddy;

import org.junit.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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
}
