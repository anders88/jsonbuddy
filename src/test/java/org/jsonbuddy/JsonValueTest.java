package org.jsonbuddy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThatThrownBy;
import org.junit.Test;

public class JsonValueTest {

    @Test
    public void shouldNotAcceptNullNumbers() throws Exception {
        assertThatThrownBy(() -> new JsonNumber(null))
            .hasMessageContaining("Use JsonNull with null");
    }

    @Test
    public void shouldEqualSameNumber() throws Exception {
        JsonNumber number = new JsonNumber(123.0);

        assertThat(number)
            .isEqualTo(number)
            .isEqualTo(number.deepClone())
            .isNotEqualTo(new JsonNumber(123))
            .isNotEqualTo(123.0);
    }

    @Test
    public void shouldSupport() throws Exception {
        JsonBoolean b = new JsonBoolean(false);

        assertThat(b).isEqualTo(b).isEqualTo(b.deepClone())
            .isNotEqualTo(false).isNotEqualTo(new JsonBoolean(true));
    }


}
