package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static io.github.daring2.hanabi.model.GameTestUtils.checkCardIndexError;
import static io.github.daring2.hanabi.model.GameTestUtils.newGame;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlayerActionTest {

    @Test
    void testExecute() {
        var game = spy(newGame());
        doNothing().when(game).checkActive();
        doNothing().when(game).checkCurrentPlayer(any());
        doNothing().when(game).startNextTurn();

        var player0 = new Player("p0");
        var actionCalls = new ArrayList<String>();
        var action = new TestPlayerAction(game, player0);
        action.action = () -> actionCalls.add("a0");

        action.execute();
        verify(game).checkActive();
        verify(game).checkCurrentPlayer(player0);
        verify(game).startNextTurn();
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

}