package io.github.daring2.hanabi.model.event;

import io.github.daring2.hanabi.model.CardInfo;
import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.Player;

public record SuggestEvent(
        Game game,
        Player player,
        Player targetPlayer,
        CardInfo info
) implements GameEvent {
}
