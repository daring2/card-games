package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;

import java.util.List;

record BotState(
        List<Game> games
) {
}
