package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.Card;
import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.Player;

public record PlayCardEvent(
        Game game,
        Player player,
        Card card,
        boolean isValid
) implements GameEvent {
}
