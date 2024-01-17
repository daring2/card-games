package io.github.daring2.hanabi.telegram;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserCommandTest {

    @Test
    void testConstructor() {
        var args1 = List.of("a1", "a2");
        assertThat(new UserCommand(args1)).satisfies(command -> {
            assertThat(command.name).isEqualTo("a1");
            assertThat(command.arguments).isEqualTo(args1);
        });
        var args2 = List.of("A1", "A2");
        assertThat(new UserCommand(args2)).satisfies(command -> {
            assertThat(command.name).isEqualTo("a1");
            assertThat(command.arguments).isEqualTo(args2);
        });
    }

}