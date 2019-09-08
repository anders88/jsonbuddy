package org.jsonbuddy;


import org.assertj.core.data.Offset;
import org.jsonbuddy.parse.JsonParseException;
import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class JsonParserTest {

    @Test
    public void shouldParseEmptyObject() throws IOException {
        JsonNode jsonNode = JsonParser.parseNode(new StringReader("{}"));
        assertThat(jsonNode.isObject()).isTrue();
        assertThat(jsonNode instanceof JsonObject).isTrue();
    }

    @Test
    public void shouldParseObjectWithStringValue() throws IOException {
        StringReader input = new StringReader(fixQuotes("{'name':'Darth Vader'}"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseNode(input);
        assertThat(jsonObject.stringValue("name")).isPresent().contains("Darth Vader");
    }

    @Test
    public void shouldHandleMultipleValuesInObject() throws IOException {
        StringReader input = new StringReader(fixQuotes("{'firstname':'Darth', 'lastname': 'Vader'}"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseNode(input);
        assertThat(jsonObject.stringValue("firstname")).isPresent().contains("Darth");
        assertThat(jsonObject.stringValue("lastname")).isPresent().contains("Vader");
    }

    @Test
    public void shouldHandleArrays() {
        JsonArray array = JsonArray.parse(fixQuotes("['one','two','three']"));
        assertThat(array.strings()).containsExactly("one", "two", "three");
    }

    @Test
    public void shouldHandleEmptyArrays() {
        JsonArray jsonArray = JsonArray.parse("[  \n\n ]");
        assertThat(jsonArray).isEmpty();
    }

    @Test
    public void shouldWarnOnWrongParseType() {
        assertThatThrownBy(() -> JsonArray.parse(fixQuotes("{'foo':'bar'}")))
                .hasMessageContaining("Expected JSON array got class org.jsonbuddy.JsonObject");
        assertThatThrownBy(() -> JsonObject.parse(fixQuotes("['foo', 'bar']")))
            .hasMessageContaining("Expected JSON object got class org.jsonbuddy.JsonArray");
    }

    @Test
    public void shouldHandleObjectWithArray() throws IOException {
        StringReader input = new StringReader(fixQuotes("{'name':'Anakin','children':['Luke','Leia']}"));
        JsonObject vader = (JsonObject) JsonParser.parseNode(input);
        List<String> children = vader.requiredArray("children").stringStream()
                .collect(Collectors.toList());
        assertThat(children).containsExactly("Luke", "Leia");
    }

    @Test
    public void shouldHandleString() {
        JsonObject obj = (JsonObject) JsonParser.parse("{}");
        assertThat(obj).isNotNull();
    }

    @Test
    public void shouldHandleBoolean() {
        JsonObject jsonObject = (JsonObject) JsonParser.parse(fixQuotes("{'boolVal':false}"));
        JsonNode boolVal = jsonObject.value("boolVal").get();

        assertThat(boolVal).isInstanceOf(JsonBoolean.class);
        assertThat(((JsonBoolean) boolVal).booleanValue()).isFalse();
    }

    @Test
    public void shouldHandleNull() {
        JsonObject jsonObject = (JsonObject) JsonParser.parse(fixQuotes("{'boolVal':null}"));
        JsonNode nullVal = jsonObject.value("boolVal").get();

        assertThat(nullVal).isInstanceOf(JsonNull.class);
    }

    @Test
    public void shouldHandleInteger() {
        JsonObject jsonObject = JsonObject.parse(fixQuotes("{'theMeaning':42}"));
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
    public void shouldHandleComplexNumbers() {
        JsonObject jsonObject = JsonObject.parse(fixQuotes("{'a':-1,'b':3.14,'c':2.5e3}"));
        assertThat(jsonObject.requiredLong("a")).isEqualTo(-1);
        assertThat(jsonObject.requiredDouble("b")).isEqualTo(3.14d);
        assertThat(jsonObject.requiredDouble("c")).isEqualTo(2500d);
    }

    @Test
    public void shouldHandleSpecialCharacters() {
        String input = fixQuotes("{'eval':'quote:\\\" backslash\\\\ \\/slash \\f bell\\b tab\\t newline\\nrest'}");
        JsonObject val = JsonObject.parse(input);

        assertThat(val.stringValue("eval").get()).isEqualTo("quote:\" backslash\\ /slash \f bell\b tab\t newline\nrest");
    }

    @Test
    public void shouldThrowExceptionIfJsonIsInvalid() {
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
    public void shouldHandleLinebreaks() {
        String jsonWithLinebreak = fixQuotes("{\n'name':'Darth',\n'title':'Dark Lord'\n}");
        JsonNode result = JsonParser.parse(jsonWithLinebreak);
        assertThat(result).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) result;
        assertThat(jsonObject.requiredString("name")).isEqualTo("Darth");
        assertThat(jsonObject.requiredString("title")).isEqualTo("Dark Lord");
    }

    @Test
    public void shouldParseToInstant() {
        JsonObject jsonObject = JsonObject.parse(fixQuotes("{'time':'2015-08-30T11:21:12.314Z'}"));
        Optional<JsonNode> time = jsonObject.value("time");
        assertThat(time).isPresent().containsInstanceOf(JsonString.class);

        JsonString jsonInstantValue = (JsonString) time.get();

        assertThat(jsonInstantValue.instantValue()).isEqualTo(LocalDateTime.of(2015, 8, 30, 13, 21, 12, 314000000).atOffset(ZoneOffset.ofHours(2)).toInstant());
    }

    @Test
    public void shouldHandleEmptyArray() {
        JsonObject jsonObject = JsonObject.parse("{\"properties\":{\"myEmptyList\":[]}}");
        assertThat(jsonObject.requiredObject("properties").requiredArray("myEmptyList")).isEmpty();
    }

    @Test
    public void shouldHandleNestedObjectFollowedByAnotherProperty() {
        JsonObject jsonObject = JsonObject.parse(fixQuotes("{'objone':{'color':'blue'},'name':'Darth Vader'}"));
        assertThat(jsonObject.requiredString("name")).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleLineshifts() {
        JsonObject.parse(fixQuotes("{'tablevalues':\n['one','two']}"));
    }

    @Test
    public void shouldHandleSpecialCharsAfterNumbers() {
        String val = "{\"id\":4326\r}";
        JsonObject jsonObject = JsonObject.parse(val);
        assertThat(jsonObject.requiredLong("id")).isEqualTo(4326);
    }

    @Test
    public void shouldHandleEmptyString() {
        JsonObject jsonObject = JsonObject.parse(fixQuotes("{'emptyString':''}"));
        assertThat(jsonObject.requiredString("emptyString")).isEqualTo("");
    }

    @Test
    public void shouldHandleNestedArrays() {
        String json = "{\"coordinates\":[[9.0, 80.0]]}";
        JsonObject jsonObject = JsonObject.parse(json);
        assertThat(jsonObject).isNotNull();
    }

    @Test
    public void shouldHandleNullElementsInArray() {
        String json = fixQuotes("['one',null,'two']");
        JsonArray jsonArray = JsonArray.parse(json);
        assertThat(jsonArray.size()).isEqualTo(3);
        assertThat(jsonArray.get(1, JsonNull.class)).isEqualTo(new JsonNull());
    }

    @Test
    public void shouldHandleUnicode() {
        JsonObject jsonObject = JsonObject.parse(fixQuotes("{'value':'with\\u22A1xx'}"));
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
        JsonObject parsed = JsonObject.parse("{\"numval\" : 0e+1}");
        assertThat(parsed.requiredDouble("numval")).isCloseTo(0d, Offset.offset(0.00001d));
    }

    private static String fixQuotes(String content) {
        return content.replace("'", "\"");
    }

}
