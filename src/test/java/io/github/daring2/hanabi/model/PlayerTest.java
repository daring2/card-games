package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

class PlayerTest {

    @Test
    void testToString() {
        var player = new Player("p0");
        assertThat(player.toString()).isEqualTo("p0");
    }

    @Test
    void testRemoveCard() {
        var player = new Player("p0");
        var cards = rangeClosed(0, 2)
                .mapToObj(i -> new Card(Color.WHITE, i))
                .toList();
        player.cards.addAll(cards);
        player.knownCards.put(cards.get(0), new CardInfo(0));
        player.knownCards.put(cards.get(1), new CardInfo(1));

        assertThat(player.removeCard(1)).isEqualTo(cards.get(1));
        assertThat(player.cards).containsExactly(
                cards.get(0), cards.get(2)
        );
        assertThat(player.knownCards).hasSize(1)
                .containsEntry(cards.get(0), new CardInfo(null, 0));

        assertThat(player.removeCard(0)).isEqualTo(cards.get(0));
        assertThat(player.cards).containsExactly(
                cards.get(2)
        );
        assertThat(player.knownCards).isEmpty();
    }

    @Test
    void testAddCardInfo() {
        var player = new Player("p0");
        assertThat(player.knownCards).isEmpty();

        var cards = new ArrayList<Card>();
        rangeClosed(1, 2).forEach(i -> cards.add(new Card(Color.RED, i)));
        rangeClosed(2, 3).forEach(i -> cards.add(new Card(Color.GREEN, i)));
        player.cards.addAll(cards);

        player.addCardInfo(new CardInfo(Color.WHITE));
        assertThat(player.knownCards).isEmpty();

        player.addCardInfo(new CardInfo(5));
        assertThat(player.knownCards).isEmpty();

        player.addCardInfo(new CardInfo(Color.RED));
        assertThat(player.knownCards).hasSize(2)
                .containsEntry(cards.get(0), new CardInfo(Color.RED))
                .containsEntry(cards.get(1), new CardInfo(Color.RED));

        player.addCardInfo(new CardInfo(2));
        assertThat(player.knownCards).hasSize(3)
                .containsEntry(cards.get(0), new CardInfo(Color.RED))
                .containsEntry(cards.get(1), new CardInfo(Color.RED, 2))
                .containsEntry(cards.get(2), new CardInfo(2));

        player.addCardInfo(new CardInfo(Color.GREEN));
        assertThat(player.knownCards).hasSize(4)
                .containsEntry(cards.get(0), new CardInfo(Color.RED))
                .containsEntry(cards.get(1), new CardInfo(Color.RED, 2))
                .containsEntry(cards.get(2), new CardInfo(Color.GREEN, 2))
                .containsEntry(cards.get(3), new CardInfo(Color.GREEN));
    }

}