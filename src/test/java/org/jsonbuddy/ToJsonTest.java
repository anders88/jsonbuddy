package org.jsonbuddy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ToJsonTest {
    @Test
    public void shouldConvertObject() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().withValue("name", JsonSimpleValueFactory.text("Darth Vader")).create();

        assertThat(jsonObject.toJson()).isEqualTo(fixQuotes("{'name':'Darth Vader'}"));

    }

    private static String fixQuotes(String content) {
        return content.replace("'", "\"");
    }

}
