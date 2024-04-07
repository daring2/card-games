package io.github.daring2.hanabi.model;

import io.github.daring2.hanabi.model.event.AddCardToTableEvent;
import io.github.daring2.hanabi.model.event.AddRedTokenEvent;
import io.github.daring2.hanabi.model.event.PlayCardEvent;
import io.github.daring2.hanabi.model.event.StartTurnEvent;
import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.model.Color.WHITE;
import static io.github.daring2.hanabi.model.GameTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PlayCardActionTest {

    @Test
    void testExecute() {
        checkGameStart(it -> {
            var game = spy(it);
            var player0 = game.players.get(0);
            game.started = false;
            checkGameNotStartedError(() -> game.playCard(player0, 0));
            game.started = true;
            checkCardIndexError(() -> game.discardCard(player0, -1));
        });
        checkGameStart(it -> {
            var game = spy(it);
            game.events.clear();
            var player0 = game.players.get(0);
            var card0 = player0.cards.get(0);
            game.playCard(player0, 0); // W-1
            assertThat(player0.cards).hasSize(5)
                    .first().isEqualTo(new Card(WHITE, 3));
            verify(game, times(1))
                    .addCardToTable(card0);
            verify(game, times(0)).addRedToken();
            assertThat(game.redTokens).isEqualTo(0);
            assertThat(game.discard).isEmpty();
            verify(game, times(1)).takeCard(player0);
            assertThat(game.events).containsExactly(
                    new PlayCardEvent(game, player0, card0, true),
                    new AddCardToTableEvent(game, card0),
                    new StartTurnEvent(game, 2)
            );
        });
        checkGameStart(it -> {
            var game = spy(it);
            game.events.clear();
            var player0 = game.players.get(0);
            var card1 = player0.cards.get(1);
            game.playCard(player0, 1); // W-3
            assertThat(player0.cards).hasSize(5)
                    .first().isEqualTo(new Card(WHITE, 1));
            verify(game, times(0))
                    .addCardToTable(new Card(WHITE, 1));
            verify(game, times(1)).addRedToken();
            assertThat(game.discard).containsExactly(card1);
            assertThat(game.redTokens).isEqualTo(1);
            verify(game, times(1)).takeCard(player0);
            assertThat(game.events).containsExactly(
                    new PlayCardEvent(game, player0, card1, false),
                    new AddRedTokenEvent(game, 1),
                    new StartTurnEvent(game, 2)
            );
        });
    }

}