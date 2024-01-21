package io.github.daring2.hanabi.telegram;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserCommandTest {

    @Test
    void testParse() {
        assertThat(UserCommand.parse("a1 a2")).satisfies(command -> {
            assertThat(command.name).isEqualTo("a1");
            assertThat(command.arguments).containsExactly("a1", "a2");
            assertThat(command.expression).isEqualTo("a1 a2");
        });
        assertThat(UserCommand.parse("/A1 /A2")).satisfies(command -> {
            assertThat(command.name).isEqualTo("a1");
            assertThat(command.arguments).containsExactly("a1", "/a2");
            assertThat(command.expression).isEqualTo("a1 /a2");
        });
    }

    @Test
    void testGetArgument() {
        var command = new UserCommand(List.of("a1", "a2"));
        assertThat(command.name).isEqualTo("a1");
        assertThat(command.getArgument(0)).isEqualTo("a1");
        assertThat(command.getArgument(1)).isEqualTo("a2");
        assertThat(command.getArgument(3)).isNull();
    }

    @Test
    void testGetIndexArgument() {
        var command = new UserCommand(List.of("a1", "2"));
        assertThat(command.getIndexArgument(0)).isEqualTo(-1);
        assertThat(command.getIndexArgument(1)).isEqualTo(1);
        assertThat(command.getIndexArgument(3)).isEqualTo(-1);
    }

}