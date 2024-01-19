package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.github.daring2.hanabi.model.GameResult.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GameMessagesTest {

    @Autowired
    GameMessages messages;

    @Test
    void testGetMessage() {
        assertThat(messages.getMessage("m1", "a1", "a2"))
                .isEqualTo("m1: [a1, a2]");
        assertThat(messages.getMessage("invalid_command", "c1"))
                .isEqualTo("Некорректная команда: c1.");
        assertThat(messages.getMessage("game_started"))
                .isEqualTo("Игра запущена.");
    }

    @Test
    void testFormatArgument() {
        assertThat(messages.formatArgument("m1")).isEqualTo("m1");
        var game = new Game();
        assertThat(messages.formatArgument(game)).isEqualTo(game.id);
        var player = new Player("p1");
        assertThat(messages.formatArgument(player)).isEqualTo("p1");
        assertThat(messages.formatArgument(WIN)).isEqualTo("фейерверк запущен");
    }

    @Test
    void testGetResultLabel() {
        assertThat(messages.getResultLabel(WIN)).isEqualTo("фейерверк запущен");
        assertThat(messages.getResultLabel(LOSS)).isEqualTo("поражение");
        assertThat(messages.getResultLabel(CANCEL)).isEqualTo("отмена");
    }

}