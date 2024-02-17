package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static io.github.daring2.hanabi.model.GameTestUtils.newGame;
import static org.assertj.core.api.Assertions.assertThat;
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

}