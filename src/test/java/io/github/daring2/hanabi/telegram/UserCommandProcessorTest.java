package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameException;
import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.telegram.BotTestUtils.newSession;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class UserCommandProcessorTest {

    @Test
    void testCheckGameNotNull() {
        var session = newSession();
        var processor1 = new UserCommandProcessor(session, null);
        assertThatExceptionOfType(GameException.class)
                .isThrownBy(processor1::checkGameNotNull)
                .withMessage("game_is_null");

        session.game = new Game();
        var processor2 = new UserCommandProcessor(session, null);
        assertThatNoException().isThrownBy(processor2::checkGameNotNull);
    }

}