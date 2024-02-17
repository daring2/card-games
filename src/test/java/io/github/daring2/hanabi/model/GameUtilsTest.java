package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.model.GameUtils.validate;
import static org.assertj.core.api.Assertions.*;

class GameUtilsTest {

    @Test
    void testValidate() {
        assertThatCode(() ->
                validate(true, "code1", "a1", "a2")
        ).doesNotThrowAnyException();
        assertThatThrownBy(() ->
                validate(false, "code1", "a1", "a2")
        ).isInstanceOfSatisfying(GameException.class, e -> {
            assertThat(e.getCode()).isEqualTo("code1");
            assertThat(e.getArguments()).containsExactly("a1", "a2");
        });
    }

}