package org.jsonbuddy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ToJsonTest {
    @Test
    public void shouldConvertObject() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().withValue("name", JsonSimpleValueFactory.text("Darth Vader")).create();

        assertThat(jsonObject.toJson()).isEqualTo(fixQuotes("{'name':'Darth Vader'}"));
    }

    @Test
    public void shouldHandleArrays() throws Exception {
        JsonArray jsonArray = JsonFactory.jsonArray().add("Luke").add("Leia").create();
        assertThat(jsonArray.toJson()).isEqualTo(fixQuotes("['Luke','Leia']"));

    }

    @Test
    public void shouldHandleSpecialCharacters() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().withValue("atext", "o\"ne\ntwo").create();
        assertThat(jsonObject.toJson()).isEqualTo(fixQuotes("{'atext':'o\\\"ne\\ntwo'}"));
    }

    private static String fixQuotes(String content) {
        return content.replace("'", "\"");
    }

}
