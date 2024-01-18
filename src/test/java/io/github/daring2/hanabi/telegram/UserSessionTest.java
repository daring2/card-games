package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameException;
import io.github.daring2.hanabi.model.Player;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class UserSessionTest {


    @Test
    void testCheckGameNotNull() {
        var session = new UserSession(null, new User(), 0L);
        assertThatExceptionOfType(GameException.class)
                .isThrownBy(session::checkGameNotNull)
                .withMessage("game_is_null");

        session.game = new Game();
        assertThatNoException().isThrownBy(session::checkGameNotNull);
    }

    @Test
    void testCheckPlayerIndex() {
        var session = new UserSession(null, new User(), 0L);
        session.game = new Game();
        session.game.addPlayer(new Player("p0"));
        session.game.addPlayer(new Player("p1"));

        checkPlayerIndexError(() -> session.checkPlayerIndex(-1));
        assertThatNoException().isThrownBy(() -> session.checkPlayerIndex(0));
        assertThatNoException().isThrownBy(() -> session.checkPlayerIndex(1));
        checkPlayerIndexError(() -> session.checkPlayerIndex(2));
    }

    void checkPlayerIndexError(ThrowingCallable action) {
        assertThatExceptionOfType(GameException.class)
                .isThrownBy(action)
                .withMessage("invalid_player_index");
    }

}