package org.jsonbuddy;

import org.junit.Test;

import java.util.List;
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
}
