package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.Player;
import io.github.daring2.hanabi.model.event.*;
import io.github.daring2.hanabi.model.event.GameEventBus.Subscription;

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

    public void process(GameEvent event) {
        switch (event) {
            case GameCreatedEvent ignored -> {
                sendMessage("game_created: %s", game);
            }
            case PlayerAddedEvent e -> {
                sendMessage("player_joined: game=%s, player=%s", game, e.player());
            }
            case PlayerRemovedEvent e -> {
                sendMessage("player_left: game=%s, player=%s", game, e.player());
            }
            case GameStartedEvent ignored -> {
                sendMessage("game_started: %s", game);
            }
            case GameFinishedEvent e -> {
                sendMessage("game_finished: game=%s, result=%s", game, e.result());
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
