package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.model.DeckFactory.DECK_SIZE;
import static org.assertj.core.api.Assertions.assertThat;

class GameFactoryTest {

    @Test
    void testCreate() {
        var context = new GameFactory.Context(
                new DeckFactory()
        );
        var factory = new GameFactory(context);
        assertThat(factory.create()).satisfies(game -> {
            assertThat(game.deck).hasSize(DECK_SIZE);
        });
    }

}