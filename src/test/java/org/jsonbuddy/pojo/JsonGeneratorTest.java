package org.jsonbuddy.pojo;

import org.jsonbuddy.*;
import org.jsonbuddy.pojo.testclasses.SimpleWithName;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonGeneratorTest {

    @Test
    public void shouldHandleSimpleClass() throws Exception {
        SimpleWithName simpleWithName = new SimpleWithName("Darth Vader");
        JsonNode generated = JsonGenerator.generate(simpleWithName);
        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        assertThat(jsonObject.stringValue("name").get()).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleSimpleValues() throws Exception {
        assertThat(JsonGenerator.generate(null)).isEqualTo(new JsonNullValue());
        assertThat(JsonGenerator.generate("Darth")).isEqualTo(JsonFactory.jsonText("Darth"));
        assertThat(JsonGenerator.generate(42)).isEqualTo(JsonFactory.jsonLong(42L));

    }

    @Test
    public void shoulHandleFloats() throws Exception {
        JsonNode jsonNode = JsonGenerator.generate(3.14f);
        JsonDouble jsonDouble = (JsonDouble) jsonNode;
        assertThat(new Double(jsonDouble.doubleValue()).floatValue()).isEqualTo(3.14f);
    }

}
