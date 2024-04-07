package io.github.daring2.hanabi.model;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.github.daring2.hanabi.model.Game.MAX_CARD_VALUE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GameTestUtils {

    public static Game newGame() {
        var cards = new ArrayList<Card>();
        for (Color color : Color.valueList) {
            for (int value = 1; value <= MAX_CARD_VALUE; value++) {
                cards.add(new Card(color, value));
            }
        }
        var game = new Game();
        game.deck.addAll(cards.reversed());
        game.addPlayer(new Player("p0"));
        game.addPlayer(new Player("p1"));
        return game;
    }

    static void checkGameStart(Consumer<Game> action) {
        var game = newGame();
        game.start();
        action.accept(game);
    }

    static void checkCardIndexError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(GameException.class)
                .hasMessage("invalid_card_index");
    }

    static void checkGameNotStartedError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(GameException.class)
                .hasMessage("game_not_started");
    }

    public static Player newPlayer(String name, List<Card> cards) {
        var player = new Player(name);
        player.cards.addAll(cards);
        return player;
    }

    public static void addKnownCard(Player player, Card card, CardInfo cardInfo) {
        player.knownCards.put(card, cardInfo);
    }

    private GameTestUtils() {
    }

}
