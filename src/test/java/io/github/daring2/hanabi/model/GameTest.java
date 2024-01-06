package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    @Test
    void testStart() {
        var deck = new DeckFactory().create();
        var game = new Game(deck);
        assertThatThrownBy(game::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid players count: 0");

        game.join(new Player("p0"));
        assertThatThrownBy(game::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid players count: 1");

        game.join(new Player("p1"));
        game.start();
        for (Color color : Color.values()) {
            assertThat(game.table.get(color)).isEmpty();;
        }
        assertThat(game.players).hasSize(2);
        for (Player player : game.players) {
            assertThat(player.cards).hasSize(5);
        }
        assertThat(game.turn).isEqualTo(0);
        assertThat(game.started).isTrue();

        assertThatThrownBy(game::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("game is started");
    }

    @Test
    void testDiscardCard() {
        //TODO implement
    }

    @Test
    void testPlayCard() {
        //TODO implement
    }

}