package io.github.daring2.hanabi.telegram;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserCommandTest {

    @Test
    void testParse() {
        assertThat(UserCommand.parse("a1 a2 a3")).satisfies(command -> {
            assertThat(command.name).isEqualTo("a1");
            assertThat(command.arguments).containsExactly("a2", "a3");
            assertThat(command.text).isEqualTo("a1 a2 a3");
        });
        assertThat(UserCommand.parse("/A1 /A2 a3")).satisfies(command -> {
            assertThat(command.name).isEqualTo("a1");
            assertThat(command.arguments).containsExactly("/A2", "a3");
            assertThat(command.text).isEqualTo("a1 /A2 a3");
        });
    }

    @Test
    void testGetArgument() {
        var command = UserCommand.parse("a1 a2 a3");
        assertThat(command.name).isEqualTo("a1");
        assertThat(command.getArgument(0)).isEqualTo("a2");
        assertThat(command.getArgument(1)).isEqualTo("a3");
        assertThat(command.getArgument(2)).isNull();
    }

    @Test
    void testGetIndexArgument() {
        var command = UserCommand.parse("a1 a2 3");
        assertThat(command.getIndexArgument(0)).isEqualTo(-1);
        assertThat(command.getIndexArgument(1)).isEqualTo(2);
        assertThat(command.getIndexArgument(2)).isEqualTo(-1);
    }

    @Test
    void testEquals() {
        var command = UserCommand.parse("a1 a2");
        assertThat(command).isEqualTo(UserCommand.parse("a1 a2"));
        assertThat(command).isNotEqualTo(UserCommand.parse("a1 a3"));
    }


}