package org.jsonbuddy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ToJsonTest {
    @Test
    public void shouldConvertObject() {
        JsonObject jsonObject = new JsonObject().put("name", new JsonString("Darth Vader"));

        assertThat(jsonObject.toJson()).isEqualTo(fixQuotes("{'name':'Darth Vader'}"));
    }

    @Test
    public void shouldHandleArrays() {
        JsonArray jsonArray = new JsonArray().add("Luke").add("Leia");
        assertThat(jsonArray.toJson()).isEqualTo(fixQuotes("['Luke','Leia']"));

    }

    @Test
    public void shouldHandleSpecialCharacters() {
        JsonObject jsonObject = new JsonObject().put("atext", new JsonString("o\"ne\ntwo"));
        assertThat(jsonObject.toJson()).isEqualTo(fixQuotes("{'atext':'o\\\"ne\\ntwo'}"));
    }

    @Test
    public void shouldIndentArraysInsideObjects() {
       JsonObject jsonObject = new JsonObject()
               .put("name", "Darth Vader")
               .put("children", new JsonArray()
                       .add("Luke")
                       .add("Leia"));
       assertThat(jsonObject.toIndentedJson("  ")).isEqualTo(
               "{\n" +
               "  \"name\":\"Darth Vader\",\n" +
               "  \"children\":[\n" +
               "    \"Luke\",\n" +
               "    \"Leia\"\n" +
               "  ]\n" +
               "}");

    }

    @Test
    public void shouldIndentObjectsInsideArrays() {
       JsonArray jsonObject = new JsonArray()
               .add(new JsonObject().put("name", "Darth Vader").put("job", "Sith Lord"))
               .add(new JsonObject().put("name", "Leia").put("job", "General"))
               .add(new JsonObject().put("name", "Luke").put("job", "Jedi"));
       assertThat(jsonObject.toIndentedJson("  ")).isEqualTo(
               "[\n" +
               "  {\n" +
               "    \"name\":\"Darth Vader\",\n" +
               "    \"job\":\"Sith Lord\"\n" +
               "  },\n" +
               "  {\n" +
               "    \"name\":\"Leia\",\n" +
               "    \"job\":\"General\"\n" +
               "  },\n" +
               "  {\n" +
               "    \"name\":\"Luke\",\n" +
               "    \"job\":\"Jedi\"\n" +
               "  }\n" +
               "]");

    }

    private static String fixQuotes(String content) {
        return content.replace("'", "\"");
    }

}
