package org.jsonbuddy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonValueTest {

    @Test
    public void shouldNotAcceptNullNumbers() {
        assertThatThrownBy(() -> new JsonNumber(null))
            .hasMessageContaining("Use JsonNull with null");
    }

    @Test
    public void shouldEqualSameNumber() {
        JsonNumber number = new JsonNumber(123.0);

        assertThat(number)
            .isEqualTo(number)
            .isEqualTo(number.deepClone())
            .isNotEqualTo(new JsonNumber(123))
            .isNotEqualTo(123.0);
    }

    @Test
    public void shouldSupport() {
        JsonBoolean b = new JsonBoolean(false);

        assertThat(b).isEqualTo(b).isEqualTo(b.deepClone())
            .isNotEqualTo(false).isNotEqualTo(new JsonBoolean(true));
    }


}
