package org.jsonbuddy;

import org.jsonbuddy.factory.JsonFactory;
import org.jsonbuddy.factory.JsonSimpleValueFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFactoryTest {
    @Test
    public void shouldCreateJsonNode() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .create();
        assertThat(jsonObject).isNotNull();
    }

    @Test
    public void shouldCreateValue() throws Exception {
        JsonSimpleValueFactory text = JsonSimpleValueFactory.text("Darth Vader");
        JsonSimpleValue jsonSimpleValue = text.create();
        assertThat(jsonSimpleValue.value()).isEqualTo("Darth Vader");

    }
}
