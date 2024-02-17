package io.github.daring2.hanabi.model;

import io.github.daring2.hanabi.model.event.FinishGameEvent;
import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.model.GameTestUtils.checkGameNotStartedError;
import static io.github.daring2.hanabi.model.GameTestUtils.newGame;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LaunchFireworksActionTest {

    @Test
    void testExecute() {
        var game = newGame();
        var player0 = game.players.getFirst();
        checkGameNotStartedError(() -> game.launchFireworks(player0));

        game.start();
        assertThatThrownBy(() -> game.launchFireworks(player0))
                .isInstanceOf(GameException.class)
                .hasMessage("deck_not_empty");

        game.events.clear();
        game.deck.clear();
        assertThat(game.result).isNull();
        game.launchFireworks(player0);
        assertThat(game.result).isEqualTo(GameResult.LAUNCH);
        assertThat(game.events).containsExactly(
                new FinishGameEvent(game, GameResult.LAUNCH, 0)
        );
    }

}