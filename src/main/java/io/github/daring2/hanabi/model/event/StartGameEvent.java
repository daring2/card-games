package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.Game;

public record StartGameEvent(
        Game game
) implements GameEvent {
}
