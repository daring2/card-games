package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameException;
import io.github.daring2.hanabi.model.Player;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.telegram.BotTestUtils.createTestSession;
import static org.assertj.core.api.Assertions.*;

class UserSessionTest {

    @Test
    void testGetPlayer() {
        var session = createTestSession();
        var game = new Game();
        var players = game.players();
        game.addPlayer(new Player("p0"));
        game.addPlayer(new Player("p1"));
        session.game = game;

        checkPlayerIndexError(() -> session.getPlayer(-1));
        assertThat(session.getPlayer(0)).isEqualTo(players.get(0));
        assertThat(session.getPlayer(1)).isEqualTo(players.get(1));
        assertThatNoException().isThrownBy(() -> session.getPlayer(1));
        checkPlayerIndexError(() -> session.getPlayer(2));
    }

    void checkPlayerIndexError(ThrowingCallable action) {
        assertThatExceptionOfType(GameException.class)
                .isThrownBy(action)
                .withMessage("invalid_player_index");
    }

}
