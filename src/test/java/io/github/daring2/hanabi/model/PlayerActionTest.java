package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static io.github.daring2.hanabi.model.GameTestUtils.checkCardIndexError;
import static io.github.daring2.hanabi.model.GameTestUtils.newGame;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerActionTest {

    @Test
    void testExecute() {
        var game = spy(newGame());
        doNothing().when(game).checkActive();

        doNothing().when(game).startNextTurn();

        var player0 = new Player("p0");
        var actionCalls = new ArrayList<String>();
        var action = spy(new TestPlayerAction(game, player0));
        doNothing().when(action).checkCurrentPlayer();
        action.action = () -> actionCalls.add("a0");

        action.execute();
        verify(game).checkActive();
        verify(game).startNextTurn();
        verify(action).checkCurrentPlayer();
        assertThat(actionCalls).containsExactly("a0");
    }

    @Test
    void testCheckCardIndex() {
        var game = newGame();
        var player0 = game.players.getFirst();
        var action = new TestPlayerAction(game, player0);
        rangeClosed(-10, 10).forEach(i ->
                checkCardIndexError(() -> action.checkCardIndex(i))
        );

        game.start();
        rangeClosed(-10, 10).forEach(i -> {
            if (i >= 0 && i < 5) {
                assertThatCode(() -> action.checkCardIndex(i))
                        .doesNotThrowAnyException();
            } else {
                checkCardIndexError(() -> action.checkCardIndex(i));
            }
        });
    }

    @Test
    void testCheckCurrentPlayer() {
        var game = newGame();

        game.turn = 1;
        checkCurrentPlayerError(game, 0, false);
        checkCurrentPlayerError(game, 1, true);

        game.turn = 2;
        checkCurrentPlayerError(game, 0, true);
        checkCurrentPlayerError(game, 1, false);
    }

    void checkCurrentPlayerError(Game game, int playerIndex, boolean error) {
        var player = game.players.get(playerIndex);
        var action = new TestPlayerAction(game, player);
        if (error) {
            assertThatThrownBy(action::checkCurrentPlayer)
                    .isInstanceOfSatisfying(GameException.class, e -> {
                        assertThat(e.getCode()).isEqualTo("player_not_current");
                        assertThat(e.getArguments()).containsExactly(game.currentPlayer());
                    });
        } else {
            action.checkCurrentPlayer();
        }
    }

}