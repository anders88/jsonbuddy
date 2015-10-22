package org.jsonbuddy;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class JsonObjectTest {
    @Test
    public void shouldGiveStringAsDouble() throws Exception {
        JsonObject obj = JsonFactory.jsonObject().put("pi", "3.14");
        double pi = obj.requiredDouble("pi");
        assertThat(pi).isEqualTo(3.14d);

    }
}
