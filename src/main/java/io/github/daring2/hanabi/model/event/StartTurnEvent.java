package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.Game;

public record StartTurnEvent(
        Game game,
        int turn
) implements GameEvent {
}
