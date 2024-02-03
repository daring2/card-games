package io.github.daring2.hanabi.telegram;

import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.telegram.CommandArguments.parseCommand;
import static org.assertj.core.api.Assertions.assertThat;

class CommandArgumentsTest {

    @Test
    void testParse() {
        assertThat(parseCommand("")).isEqualTo(CommandArguments.EMPTY);
        assertThat(parseCommand("  ")).isEqualTo(CommandArguments.EMPTY);
        assertThat(parseCommand("a1 a2 a3")).satisfies(command -> {
            assertThat(command.name()).isEqualTo("a1");
            assertThat(command.arguments()).containsExactly("a1", "a2", "a3");
        });
        assertThat(parseCommand("/A1 /A2")).satisfies(command -> {
            assertThat(command.name()).isEqualTo("a1");
            assertThat(command.arguments()).containsExactly("a1", "/A2");
        });
    }

    @Test
    void testName() {
        assertThat(parseCommand("").name()).isNull();
        assertThat(parseCommand("a1 a2").name()).isEqualTo("a1");
    }

    @Test
    void testSize() {
        assertThat(parseCommand("").size()).isEqualTo(0);
        assertThat(parseCommand("a1").size()).isEqualTo(1);
        assertThat(parseCommand("a1 a2").size()).isEqualTo(2);
    }

    @Test
    void testIsEmpty() {
        assertThat(parseCommand("").isEmpty()).isTrue();
        assertThat(parseCommand("a1").isEmpty()).isFalse();
        assertThat(parseCommand("a1 a2").isEmpty()).isFalse();
    }

    @Test
    void testGetArgument() {
        var command = parseCommand("a1 a2 a3");
        assertThat(command.name()).isEqualTo("a1");
        assertThat(command.get(0)).isEqualTo("a1");
        assertThat(command.get(1)).isEqualTo("a2");
        assertThat(command.get(2)).isEqualTo("a3");
        assertThat(command.get(3)).isNull();
    }

    @Test
    void testGetIndexArgument() {
        var command = parseCommand("a1 a2 3");
        assertThat(command.getIndexValue(0)).isEqualTo(-1);
        assertThat(command.getIndexValue(1)).isEqualTo(-1);
        assertThat(command.getIndexValue(2)).isEqualTo(2);
        assertThat(command.getIndexValue(3)).isEqualTo(-1);
    }

    @Test
    void testBuildText() {
        var command1 = parseCommand("a1");
        assertThat(command1.buildText()).isEqualTo("a1");
        var command2 = parseCommand("a1 a2 a3");
        assertThat(command2.buildText()).isEqualTo("a1 a2 a3");
    }

    @Test
    void testEquals() {
        var command = parseCommand("a1 a2");
        assertThat(command).isEqualTo(parseCommand("a1 a2"));
        assertThat(command).isNotEqualTo(parseCommand("a1 a3"));
    }

}