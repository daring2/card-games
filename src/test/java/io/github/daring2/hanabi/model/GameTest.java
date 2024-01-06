package io.github.daring2.hanabi.model;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

import static io.github.daring2.hanabi.model.Game.MAX_BLUE_TOKENS;
import static io.github.daring2.hanabi.model.Game.MAX_FIREWORKS;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    @Test
    void testStart() {
        var game = newGame();
        game.players.clear();
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
            assertThat(game.table.get(color)).singleElement()
                    .isEqualTo(new Card(color, 0));
        }
        assertThat(game.players).hasSize(2);
        for (Player player : game.players) {
            assertThat(player.cards).hasSize(5);
        }
        assertThat(game.turn).isEqualTo(1);
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

    @Test
    void testPerformPlayerAction() {
        //TODO implement
    }

    @Test
    void testAddCardToTable() {
        var cards = rangeClosed(0, 5)
                .mapToObj(i -> new Card(Color.WHITE, i))
                .toList();
        checkAddCardToTable(game -> {
            game.addCardToTable(cards.get(1));
            assertThat(game.table.get(Color.WHITE)).isEqualTo(cards.subList(0, 2));
            assertThat(game.fireworks).isEqualTo(0);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isNull();

            game.addCardToTable(cards.get(2));
            assertThat(game.table.get(Color.WHITE)).isEqualTo(cards.subList(0, 3));
            assertThat(game.fireworks).isEqualTo(0);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isNull();
        });
        checkAddCardToTable(game -> {
            game.addCardToTable(cards.get(5));
            assertThat(game.table.get(Color.WHITE))
                    .containsExactly(cards.get(0), cards.get(5));
            assertThat(game.fireworks).isEqualTo(1);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isNull();
        });
        checkAddCardToTable(game -> {
            game.fireworks = MAX_FIREWORKS - 1;
            game.addCardToTable(cards.get(5));
            assertThat(game.table.get(Color.WHITE))
                    .containsExactly(cards.get(0), cards.get(5));
            assertThat(game.fireworks).isEqualTo(MAX_FIREWORKS);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isEqualTo(GameResult.WIN);
        });

    }

    void checkAddCardToTable(Consumer<Game> action) {
        var game = newGame();
        game.start();
        action.accept(game);
    }

    @Test
    void testDiscardRedToken() {
        var game = newGame();
        assertThat(game.redTokens).isEqualTo(3);
        assertThat(game.result).isNull();

        game.discardRedToken();
        assertThat(game.redTokens).isEqualTo(2);
        assertThat(game.result).isNull();

        game.discardRedToken();
        game.discardRedToken();
        assertThat(game.redTokens).isEqualTo(0);
        assertThat(game.result).isEqualTo(GameResult.LOSS);
    }

    @Test
    void testTakeCard() {
        var game = newGame();
        var cards = new ArrayList<>(game.deck.cards);
        Collections.reverse(cards);
        var player0 = game.players.get(0);
        player0.cards.clear();
        assertThat(player0.cards).isEmpty();

        game.takeCard(player0);
        assertThat(player0.cards).isEqualTo(cards.subList(0, 1));
        game.takeCard(player0);
        assertThat(player0.cards).isEqualTo(cards.subList(0, 2));
        game.result = GameResult.WIN;
        game.takeCard(player0);
        assertThat(player0.cards).isEqualTo(cards.subList(0, 2));
    }

    @Test
    void testStartNextTurn() {
        var game = newGame();
        checkGameNotStartedError(game::startNextTurn);
        game.started = true;
        assertThat(game.turn).isEqualTo(0);
        game.startNextTurn();
        assertThat(game.turn).isEqualTo(1);
        game.startNextTurn();
        assertThat(game.turn).isEqualTo(2);
        game.result = GameResult.WIN;
        checkGameFinishedError(game::startNextTurn);
    }

    @Test
    void testGetCurrentPlayer() {
        var game = newGame();
        var players = game.players;
        game.turn = 1;
        assertThat(game.getCurrentPlayer()).isEqualTo(players.get(0));
        game.turn = 2;
        assertThat(game.getCurrentPlayer()).isEqualTo(players.get(1));
        game.turn = 1;
        assertThat(game.getCurrentPlayer()).isEqualTo(players.get(0));
    }

    @Test
    void testCheckNotStarted() {
        var game = newGame();
        game.checkNotStarted();

        game.started = true;
        assertThatThrownBy(game::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("game is started");
    }

    @Test
    void testCheckActive() {
        var game = newGame();
        checkGameNotStartedError(game::checkActive);

        game.started = true;
        game.checkActive();

        game.result = GameResult.WIN;
        checkGameFinishedError(game::checkActive);
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
        if (error) {
            assertThatThrownBy(() -> game.checkCurrentPlayer(player))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("player '" + player + "' is not current");
        } else {
            game.checkCurrentPlayer(player);
        }
    }

    void checkGameNotStartedError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("game is not started");
    }

    void checkGameFinishedError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("game is finished");
    }

    Game newGame() {
        var deck = new DeckFactory().create();
        var game = new Game(deck);
        game.join(new Player("p0"));
        game.join(new Player("p1"));
        return game;
    }

}