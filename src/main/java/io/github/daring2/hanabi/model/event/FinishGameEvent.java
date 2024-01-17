package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameResult;

public record FinishGameEvent(
        Game game,
        GameResult result
) implements GameEvent {
}
