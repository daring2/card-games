package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameResult;
import io.github.daring2.hanabi.model.event.*;
import io.github.daring2.hanabi.model.event.GameEventBus.Subscription;

public class GameEventProcessor implements AutoCloseable {

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
            case GameCreatedEvent e -> {
                session.sendMessage("game_created", e.game());
            }
            case PlayerAddedEvent e -> {
                if (e.player() == session.player) {
                    session.sendMessage("current_player_joined");
                } else {
                    session.sendMessage("player_joined", e.player());
                }
            }
            case PlayerRemovedEvent e -> {
                if (e.player() == session.player) {
                    session.sendMessage("current_player_left");
                } else {
                    session.sendMessage("player_left", e.player());
                }
            }
            case GameStartedEvent ignored -> {
                session.sendMessage("game_started");
            }
            case GameFinishedEvent e -> {
                if (e.result() == GameResult.CANCEL) {
                    session.sendMessage("game_canceled");
                } else {
                    session.sendMessage("game_finished", e.result());
                }
            }
            case TurnStartedEvent e -> processTurnStarted(e);
            default -> {}
        }
    }

    void processTurnStarted(TurnStartedEvent event) {
        var turnInfo = session.messages().getMessage(
                "turn_info",
                game.currentPlayer(),
                game.deckSize(),
                game.blueTokens(),
                game.redTokens(),
                buildCardsInfo()
        );
        session.sendText(turnInfo);
    }

    String buildCardsInfo() {
        var table = new CardTable();
        table.addRow("table", game.tableCards());
        game.players().forEach(player -> {
            table.addRow(player.name(), player.cards());
        });
        return table.buildText();
    }

    public void close() {
        subscription.remove();
    }

}
