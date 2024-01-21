package io.github.daring2.hanabi.telegram;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserCommandTest {

    @Test
    void testParse() {
        //TODO implement
    }

    @Test
    void testConstructor() {
        var args1 = List.of("a1", "a2");
        assertThat(new UserCommand(args1)).satisfies(command -> {
            assertThat(command.name).isEqualTo("a1");
            assertThat(command.arguments).isEqualTo(args1);
            assertThat(command.expression).isEqualTo("a1 a2");
        });
        var args2 = List.of("/A1", "A2");
        assertThat(new UserCommand(args2)).satisfies(command -> {
            assertThat(command.name).isEqualTo("a1");
            assertThat(command.arguments).isEqualTo(args2);
            assertThat(command.expression).isEqualTo("/A1 A2");
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