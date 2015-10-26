package org.jsonbuddy;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThatThrownBy;

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
    public void shouldCreateFromStrings() throws Exception {
        JsonArray jsonArray = JsonArray.fromStringList(Arrays.asList("a", "b", "c"));

        assertThat(jsonArray.get(0, JsonString.class).stringValue()).isEqualTo("a");
        assertThat(jsonArray.get(1, JsonValue.class).stringValue()).isEqualTo("b");
    }

    @Test
    public void hasNoTextValue() throws Exception {
        assertThatThrownBy(() -> new JsonArray().stringValue())
            .hasMessageContaining("Not supported");
        assertThatThrownBy(() -> new JsonArray().requiredString("foo"))
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
        assertThat(array.mapNodes(n -> n.as(JsonObject.class).requiredString("a").length()))
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
}
