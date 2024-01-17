package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.Card;
import io.github.daring2.hanabi.model.Game;

public record FireworkCreatedEvent(
        Game game,
        Card card
) implements GameEvent {
}
