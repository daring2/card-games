package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.Card;
import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.Player;

public record CardPlayedEvent(
        Game game,
        Player player,
        Card card
) implements GameEvent {
}
