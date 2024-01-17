package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.Game;

public record AddRedTokenEvent(
        Game game,
        int redTokens
) implements GameEvent{
}
