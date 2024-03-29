package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.Player;

public record RemovePlayerEvent(
        Game game,
        Player player
) implements GameEvent {
}
