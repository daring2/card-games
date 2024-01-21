package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.Game;

public record DeckEmptyEvent(
        Game game
) implements GameEvent {
}
