package io.github.daring2.hanabi.model;

import io.github.daring2.hanabi.model.event.StartTurnEvent;
import io.github.daring2.hanabi.model.event.SuggestEvent;
import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.model.Color.WHITE;
import static io.github.daring2.hanabi.model.GameTestUtils.checkGameNotStartedError;
import static io.github.daring2.hanabi.model.GameTestUtils.newGame;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuggestActionTest {

    @Test
    void testExecute() {
        var game = newGame();
        var player0 = game.players.get(0);
        var player1 = game.players.get(1);
        var cardInfo = new CardInfo(WHITE);
        checkGameNotStartedError(() -> game.suggest(player0, player1, cardInfo));

        game.start();
        assertThatThrownBy(() -> game.suggest(player0, player1, new CardInfo(WHITE, 1)))
                .isInstanceOf(GameException.class)
                .hasMessage("invalid_suggestion");

        game.blueTokens = 0;
        assertThatThrownBy(() -> game.suggest(player0, player1, cardInfo))
                .isInstanceOf(GameException.class)
                .hasMessage("no_blue_tokens_available");

        game.blueTokens = 3;
        assertThatThrownBy(() -> game.suggest(player0, player0, cardInfo))
                .isInstanceOf(GameException.class)
                .hasMessage("invalid_target_player");


        game.events.clear();
        game.suggest(player0, player1, cardInfo);
        assertThat(game.blueTokens).isEqualTo(2);
        assertThat(game.events).containsExactly(
                new SuggestEvent(game, player0, player1, cardInfo),
                new StartTurnEvent(game, 2)
        );
    }

}