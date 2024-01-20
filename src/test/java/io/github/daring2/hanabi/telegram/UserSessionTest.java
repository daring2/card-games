package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.*;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.User;

import static io.github.daring2.hanabi.model.Color.*;
import static org.assertj.core.api.Assertions.*;

class UserSessionTest {


    @Test
    void testCheckGameNotNull() {
        var session = newSession();
        assertThatExceptionOfType(GameException.class)
                .isThrownBy(session::checkGameNotNull)
                .withMessage("game_is_null");

        session.game = new Game();
        assertThatNoException().isThrownBy(session::checkGameNotNull);
    }

    @Test
    void testGetPlayer() {
        var session = newSession();
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

    @Test
    void testParseCardInfo() {
        var session = newSession();
        checkInvalidSuggestionError(() -> session.parseCardInfo(null));
        checkInvalidSuggestionError(() -> session.parseCardInfo(""));

        checkInvalidSuggestionError(() -> session.parseCardInfo("0"));
        assertThat(session.parseCardInfo("1")).isEqualTo(new CardInfo(1));
        assertThat(session.parseCardInfo("5")).isEqualTo(new CardInfo(5));
        checkInvalidSuggestionError(() -> session.parseCardInfo("6"));

        checkInvalidSuggestionError(() -> session.parseCardInfo("A"));
        assertThat(session.parseCardInfo("W")).isEqualTo(new CardInfo(WHITE));
        assertThat(session.parseCardInfo("w")).isEqualTo(new CardInfo(WHITE));
        assertThat(session.parseCardInfo("Y")).isEqualTo(new CardInfo(YELLOW));
        checkInvalidSuggestionError(() -> session.parseCardInfo("Z"));
    }

    void checkInvalidSuggestionError(ThrowingCallable action) {
        assertThatExceptionOfType(GameException.class)
                .isThrownBy(action)
                .withMessage("invalid_suggestion");
    }

    UserSession newSession() {
        return new UserSession(null, new User(), 0L);
    }

}