package org.jsonbuddy;

import org.jsonbuddy.factory.JsonFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFactoryTest {
    @Test
    public void shouldCreateJsonNode() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .create();
        assertThat(jsonObject).isNotNull();
    }

    
}
