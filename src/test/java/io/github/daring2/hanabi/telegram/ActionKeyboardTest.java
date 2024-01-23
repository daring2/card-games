package io.github.daring2.hanabi.telegram;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionKeyboardTest {

    @Test
    void testBuildButtonData() {
        var command = UserCommand.parse("a1 a2 a3");
        var keyboard = new ActionKeyboard(null, command);
        assertThat(keyboard.buildButtonData(0, "v1")).isEqualTo("a1 v1");
        assertThat(keyboard.buildButtonData(1, "v2")).isEqualTo("a1 a2 v2");
    }

}