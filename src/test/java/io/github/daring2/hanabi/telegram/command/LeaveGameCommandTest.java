package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.model.Game;
import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.telegram.BotTestUtils.createTestSession;
import static io.github.daring2.hanabi.telegram.BotTestUtils.setGame;
import static org.assertj.core.api.Assertions.assertThat;

class LeaveGameCommandTest {

    @Test
    void testVisibleInMenu() {
        var session = createTestSession();
        var command = new LeaveGameCommand(session);
        setGame(session, null);
        assertThat(command.isVisibleInMenu()).isFalse();
        setGame(session, new Game());
        assertThat(command.isVisibleInMenu()).isTrue();
        setGame(session, null);
        assertThat(command.isVisibleInMenu()).isFalse();
    }


}