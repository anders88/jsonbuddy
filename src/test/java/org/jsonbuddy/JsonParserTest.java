package org.jsonbuddy;


import org.assertj.core.data.Offset;
import org.jsonbuddy.parse.JsonParseException;
import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    public void shouldHandleString() throws Exception {
        JsonObject obj = (JsonObject) JsonParser.parse("{}");
        assertThat(obj).isNotNull();
    }

    @Test
    public void shouldHandleBoolean() throws Exception {
        JsonObject jsonObject = (JsonObject) JsonParser.parse(fixQuotes("{'boolVal':false}"));
        JsonNode boolVal = jsonObject.value("boolVal").get();

        assertThat(boolVal).isInstanceOf(JsonBoolean.class);
        assertThat(((JsonBoolean) boolVal).booleanValue()).isFalse();
    }

    @Test
    public void shouldHandleNull() throws Exception {
        JsonObject jsonObject = (JsonObject) JsonParser.parse(fixQuotes("{'boolVal':null}"));
        JsonNode nullVal = jsonObject.value("boolVal").get();

        assertThat(nullVal).isInstanceOf(JsonNull.class);
    }

    @Test
    public void shouldHandleInteger() throws Exception {
        JsonObject jsonObject = JsonParser.parseToObject(fixQuotes("{'theMeaning':42}"));
        JsonNode theMeaning = jsonObject.value("theMeaning").get();
        assertThat(theMeaning).isInstanceOf(JsonNumber.class);
        JsonNumber intVal = (JsonNumber) theMeaning;
        assertThat(intVal.intValue()).isEqualTo(42);
        assertThat(intVal.longValue()).isEqualTo(42);
        assertThat(intVal.shortValue()).isEqualTo((short) 42);
        assertThat(intVal.floatValue()).isEqualTo(42.0f);
        assertThat(intVal.byteValue()).isEqualTo((byte) 42);

        JsonNode jsonNode = JsonParser.parse("42");
        assertThat(jsonNode).isEqualTo(new JsonNumber(42L));
    }

    @Test
    public void shouldHandleComplexNumbers() throws Exception {
        JsonObject jsonObject = JsonParser.parseToObject(fixQuotes("{'a':-1,'b':3.14,'c':2.5e3}"));
        assertThat(jsonObject.requiredLong("a")).isEqualTo(-1);
        assertThat(jsonObject.requiredDouble("b")).isEqualTo(3.14d);
        assertThat(jsonObject.requiredDouble("c")).isEqualTo(2500d);
    }

    @Test
    public void shouldHandleSpecialCharacters() throws Exception {
        String input = fixQuotes("{'aval':'quote:\\\" backslash\\\\ \\/slash \\f bell\\b tab\\t newline\\nrest'}");
        JsonObject val = JsonParser.parseToObject(input);

        assertThat(val.stringValue("aval").get()).isEqualTo("quote:\" backslash\\ /slash \f bell\b tab\t newline\nrest");
    }

    @Test
    public void shouldThrowExceptionIfJsonIsInvalid() throws Exception {
        validateException("{'name':'Darth Vader'", "JsonObject not closed. Expected }");
        validateException("{'name':'Darth Vader' :", "JsonObject not closed. Expected }");
        validateException("['Luke'", "Expected , or ] in array");
        validateException("{'name'}", "Expected value for objectkey name");
        validateException("{'name' 'Darth'", "Expected value for objectkey name");
        validateException("[", "Expected , or ] in array");
        validateException("[1 2]", "Expected , or ] in array");
        validateException("[1, 2", "Expected , or ] in array");
        validateException("[1, 2 :", "Expected , or ] in array");
        validateException("{'dummy':2gh}", "Illegal value '2g'");

        validateException("{'name':'Luke}", "JsonString not closed. Expected \"");
        validateException("{'name':'Luke", "JsonString not closed. Expected \"");
        validateException("{'name':Luke}", "Unexpected character 'L'");
        validateException("{'foo':", "Expected value for key foo");
        validateException("'aborted escape \\", "JsonString not closed. Ended in escape sequence");
        validateException("unquoted", "Unexpected character 'u'");
        validateException("foo", "Unexpected value foo");
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

        assertThat(jsonInstantValue.instantValue()).isEqualTo(LocalDateTime.of(2015, 8, 30, 13, 21, 12, 314000000).atOffset(ZoneOffset.ofHours(2)).toInstant());
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

    @Test
    public void shouldHandleEmptyString() throws Exception {
        JsonObject jsonObject = JsonParser.parseToObject(fixQuotes("{'emptyString':''}"));
        assertThat(jsonObject.requiredString("emptyString")).isEqualTo("");
    }

    @Test
    public void shouldHandleNestedArrays() throws Exception {
        String json = "{\"coordinates\":[[9.0, 80.0]]}";
        JsonObject jsonObject = JsonParser.parseToObject(json);
        assertThat(jsonObject).isNotNull();
    }

    @Test
    public void shouldHandleNullElementsInArray() throws Exception {
        String json = fixQuotes("['one',null,'two']");
        JsonArray jsonArray = JsonParser.parseToArray(json);
        assertThat(jsonArray.size()).isEqualTo(3);
        assertThat(jsonArray.get(1, JsonNull.class)).isEqualTo(new JsonNull());
    }

    @Test
    public void shouldHandleUnicode() throws Exception {
        JsonObject jsonObject = JsonParser.parseToObject(fixQuotes("{'value':'with\\u22A1xx'}"));
        assertThat(jsonObject.requiredString("value")).isEqualTo("with\u22A1xx");

    }

    @Test
    public void shouldParseBase64EncodedJsonArray() {
        JsonArray expected = new JsonArray().add(new JsonObject().put("Some", "value"));
        String base64EncodedString = Base64.getEncoder().encodeToString(expected.toJson().getBytes());
        JsonArray jsonArray = (JsonArray) JsonParser.parseFromBase64encodedString(base64EncodedString);
        assertThat(jsonArray).isEqualTo(expected);
    }

    @Test
    public void shouldThrowExceptionWhenInputNotJson() {
        String notJsonString = "Some value";
        String base64EncodedString = Base64.getEncoder().encodeToString(notJsonString.getBytes());
        assertThatThrownBy(() -> JsonParser.parseFromBase64encodedString(base64EncodedString))
                .isInstanceOf(JsonParseException.class);
    }

    @Test
    public void shouldThrowExceptionWhenInputNotBase64Encoded() {
        JsonObject expected = new JsonObject().put("one", "two");
        assertThatThrownBy(() -> JsonParser.parseFromBase64encodedString(expected.toJson()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldHandleNumbersWithExponent() {
        JsonObject parsed = JsonParser.parseToObject("{\"numval\" : 0e+1}");
        assertThat(parsed.requiredDouble("numval")).isCloseTo(0d, Offset.offset(0.00001d));
    }

    private static String fixQuotes(String content) {
        return content.replace("'", "\"");
    }

}