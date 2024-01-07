package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

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
        var cards = rangeClosed(1, 3)
                .mapToObj(i -> new Card(Color.WHITE, i))
                .toList();
        player.cards.addAll(cards);

        assertThat(player.removeCard(1)).isEqualTo(cards.get(1));
        assertThat(player.cards).containsExactly(
                cards.get(0), cards.get(2)
        );
        assertThat(player.removeCard(0)).isEqualTo(cards.get(0));
        assertThat(player.cards).containsExactly(
                cards.get(2)
        );
    }

}