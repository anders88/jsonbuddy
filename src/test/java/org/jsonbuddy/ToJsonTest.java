package org.jsonbuddy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ToJsonTest {
    @Test
    public void shouldConvertObject() throws Exception {
        JsonObject jsonObject = new JsonObject().withValue("name", new JsonTextValue("Darth Vader"));

        assertThat(jsonObject.toJson()).isEqualTo(fixQuotes("{'name':'Darth Vader'}"));
    }

    @Test
    public void shouldHandleArrays() throws Exception {
        JsonArray jsonArray = new JsonArray().add("Luke").add("Leia");
        assertThat(jsonArray.toJson()).isEqualTo(fixQuotes("['Luke','Leia']"));

    }

    @Test
    public void shouldHandleSpecialCharacters() throws Exception {
        JsonObject jsonObject = new JsonObject().withValue("atext", new JsonTextValue("o\"ne\ntwo"));
        assertThat(jsonObject.toJson()).isEqualTo(fixQuotes("{'atext':'o\\\"ne\\ntwo'}"));
    }

    private static String fixQuotes(String content) {
        return content.replace("'", "\"");
    }

}
