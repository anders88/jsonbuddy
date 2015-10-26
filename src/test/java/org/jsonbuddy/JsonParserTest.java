package org.jsonbuddy;


import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThatThrownBy;

public class JsonParserTest {

    @Test
    public void shouldParseEmptyObject() throws Exception {
        JsonNode jsonNode = JsonParser.parse(new StringReader("{}"));
        assertThat(jsonNode instanceof JsonObject).isTrue();
    }

    @Test
    public void shouldParseObjectWithStringValue() throws Exception {
        StringReader input = new StringReader(fixQuotes("{'name':'Darth Vader'}"));
        JsonObject jsonObject = (JsonObject) JsonParser.parse(input);
        assertThat(jsonObject.stringValue("name")).isPresent().contains("Darth Vader");
    }

    @Test
    public void shouldHandleMultipleValuesInObject() throws Exception {
        StringReader input = new StringReader(fixQuotes("{'firstname':'Darth', 'lastname': 'Vader'}"));
        JsonObject jsonObject = (JsonObject) JsonParser.parse(input);
        assertThat(jsonObject.stringValue("firstname")).isPresent().contains("Darth");
        assertThat(jsonObject.stringValue("lastname")).isPresent().contains("Vader");
    }

    @Test
    public void shouldHandleArrays() throws Exception {
        JsonArray array = JsonParser.parseToArray(fixQuotes("['one','two','three']"));
        assertThat(array.strings()).containsExactly("one", "two", "three");
    }

    @Test
    public void shouldWarnOnWrongParseType() throws Exception {
        assertThatThrownBy(() -> JsonParser.parseToArray(fixQuotes("{'foo':'bar'}")))
                .hasMessageContaining("Expected json array got class org.jsonbuddy.JsonObject");
        assertThatThrownBy(() -> JsonParser.parseToObject(fixQuotes("['foo', 'bar']")))
            .hasMessageContaining("Expected json object got class org.jsonbuddy.JsonArray");
    }

    @Test
    public void shouldHandleObjectWithArray() throws Exception {
        StringReader input = new StringReader(fixQuotes("{'name':'Anakin','children':['Luke','Leia']}"));
        JsonObject vader = (JsonObject) JsonParser.parse(input);
        List<String> children = vader.requiredArray("children").stringStream()
                .collect(Collectors.toList());
        assertThat(children).containsExactly("Luke", "Leia");
    }

    @Test
    public void shouldHandleInputStream() throws Exception {
        JsonObject obj = (JsonObject) JsonParser.parse(new ByteArrayInputStream("{}".getBytes()));
        assertThat(obj).isNotNull();
    }

    @Test
    public void shouldHandleString() throws Exception {
        JsonObject obj = (JsonObject) JsonParser.parse("{}");
        assertThat(obj).isNotNull();
    }

    @Test
    public void shouldHandleBoolean() throws Exception {
        JsonObject jsonObject = (JsonObject) JsonParser.parse(fixQuotes("{'boolVal':false}"));
        JsonNode boolVal = jsonObject.value("boolVal").get();

        assertThat(boolVal).isInstanceOf(JsonBoolean.class);
        assertThat(((JsonBoolean) boolVal).boolValue()).isFalse();
    }

    @Test
    public void shouldHandleNull() throws Exception {
        JsonObject jsonObject = (JsonObject) JsonParser.parse(fixQuotes("{'boolVal':null}"));
        JsonNode nullVal = jsonObject.value("boolVal").get();

        assertThat(nullVal).isInstanceOf(JsonNull.class);
    }

    @Test
    public void shouldHandleInteger() throws Exception {
        JsonObject jsonObject = JsonParser.parse(fixQuotes("{'theMeaning':42}")).as(JsonObject.class);
        JsonNode theMeaning = jsonObject.value("theMeaning").get();
        assertThat(theMeaning).isInstanceOf(JsonNumber.class);
        JsonNumber intVal = (JsonNumber) theMeaning;
        assertThat(intVal.intValue()).isEqualTo(42);
        assertThat(intVal.shortValue()).isEqualTo((short)42);
        assertThat(intVal.floatValue()).isEqualTo(42.0f);
        assertThat(intVal.byteValue()).isEqualTo((byte)42);
    }

    @Test
    public void shouldHandleComplexNumbers() throws Exception {
        JsonObject jsonObject = JsonParser.parse(fixQuotes("{'a':-1,'b':3.14,'c':2.5e3}")).as(JsonObject.class);
        assertThat(jsonObject.value("a").get().as(JsonNumber.class).longValue()).isEqualTo(-1);
        assertThat(jsonObject.value("b").get().as(JsonNumber.class).doubleValue()).isEqualTo(3.14d);
        assertThat(jsonObject.value("c").get().as(JsonNumber.class).doubleValue()).isEqualTo(2500d);
    }

    @Test
    public void shouldHandleSpecialCharacters() throws Exception {
        String input = fixQuotes("{'aval':'quote:\\\" backslash\\\\ /slash \\f bell\\b tab\\t newline\\nrest'}");
        JsonObject val = JsonParser.parseToObject(input);

        assertThat(val.stringValue("aval").get()).isEqualTo("quote:\" backslash\\ /slash \f bell\b tab\t newline\nrest");
    }

    @Test
    public void shouldThrowExceptionIJsonIsInvalid() throws Exception {
        validateException("{'name':'Darth Vader'", "JsonObject not closed. Expected }");
        validateException("['Luke'", "Expected , or ] in array");
        validateException("{'name'}", "Expected value for objectkey name");
        validateException("{'name' 'Darth'", "Expected value for objectkey name");
        validateException("[1 2]", "Expected , or ] in array");
        validateException("{'dummy':2gh}", "Illegal value '2g'");

        validateException("{'name':'Luke}",
                "JsonObject not closed. Expected }"); // Should be "string not closed"
        validateException("{'name':'Luke",
                "JsonObject not closed. Expected }"); // Should be "string not closed"
        validateException("{'name':Luke}",
                "JsonObject not closed. Expected }"); // Should be "unquoted string"
        // Creates a JsonNull!
        //validateException("unquoted", "Unquoted string");
        // Creates a new JsonBoolean(false)
        //validateException("foo", "Unquoted string");
    }

    private void validateException(String json, String errormessage) {
        assertThatThrownBy(() -> JsonParser.parse(fixQuotes(json)))
            .hasMessage(errormessage);
    }

    @Test
    public void shouldHandleLinebreaks() throws Exception {
        String jsonWithLinebreak = fixQuotes("{\n'name':'Darth',\n'title':'Dark Lord'\n}");
        JsonNode result = JsonParser.parse(jsonWithLinebreak);
        assertThat(result).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) result;
        assertThat(jsonObject.requiredString("name")).isEqualTo("Darth");
        assertThat(jsonObject.requiredString("title")).isEqualTo("Dark Lord");
    }

    @Test
    public void shouldParseToInstant() throws Exception {
        JsonObject jsonObject = JsonParser.parseToObject(fixQuotes("{'time':'2015-08-30T11:21:12.314Z'}"));
        Optional<JsonNode> time = jsonObject.value("time");
        assertThat(time).isPresent().containsInstanceOf(JsonString.class);

        JsonString jsonInstantValue = (JsonString) time.get();

        assertThat(jsonInstantValue.instantValue()).isEqualTo(LocalDateTime.of(2015, 8, 30, 13, 21, 12,314000000).atOffset(ZoneOffset.ofHours(2)).toInstant());
    }

    @Test
    public void shouldHandleEmptyArray() throws Exception {
        JsonObject jsonObject = JsonParser.parseToObject("{\"properties\":{\"myEmptyList\":[]}}");
        assertThat(jsonObject.requiredObject("properties").requiredArray("myEmptyList")).isEmpty();
    }

    @Test
    public void shouldHandleNestedObjectFollowedByAnotherProperty() throws Exception {
        JsonObject jsonObject = JsonParser.parseToObject(fixQuotes("{'objone':{'color':'blue'},'name':'Darth Vader'}"));
        assertThat(jsonObject.requiredString("name")).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleLineshifts() throws Exception {
        JsonParser.parseToObject(fixQuotes("{'tablevalues':\n['one','two']}"));
    }

    @Test
    public void shouldHandleSpecialCharsAfterNumbers() throws Exception {
        String val = "{\"id\":4326\r}";
        JsonObject jsonObject = JsonParser.parseToObject(val);
        assertThat(jsonObject.requiredLong("id")).isEqualTo(4326);
    }

    private static String fixQuotes(String content) {
        return content.replace("'", "\"");
    }


}