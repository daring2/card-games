package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.Game;

public record GameCreatedEvent(
        Game game
) implements GameEvent {
}
