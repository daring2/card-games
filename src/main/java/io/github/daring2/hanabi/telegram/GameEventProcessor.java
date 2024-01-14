package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.Player;
import io.github.daring2.hanabi.model.event.GameCreatedEvent;
import io.github.daring2.hanabi.model.event.GameEvent;
import io.github.daring2.hanabi.model.event.GameEventBus.Subscription;
import io.github.daring2.hanabi.model.event.GameStartedEvent;
import io.github.daring2.hanabi.model.event.PlayerAddedEvent;

import java.util.Arrays;

public class GameEventProcessor {

    final UserSession session;
    final Game game;
    final Subscription subscription;

    public GameEventProcessor(UserSession session) {
        this.session = session;
        game = session.game;
        subscription = game.eventBus().subscribe(this::process);
    }

    Player player() {
        return session.player;
    }

    public void process(GameEvent event) {
        switch (event) {
            case GameCreatedEvent ignored -> {
                sendMessage("game_created: %s", game);
            }
            case PlayerAddedEvent ignored -> {
                sendMessage("player_joined: game=%s, player=%s", game, player());
            }
            case GameStartedEvent ignored -> {
                sendMessage("game_started: %s", game);
            }
            default -> {
            }
        }
    }

    void sendMessage(String format, Object... args) {
        args = Arrays.stream(args)
                .map(this::formatMessageArgument)
                .toArray();
        session.sendMessage(format, args);
    }

    Object formatMessageArgument(Object argument) {
        if (argument == null)
            return null;
        return switch (argument) {
            case Game it -> it.id();
            case Player it -> it.name();
            default -> argument;
        };
    }

}
