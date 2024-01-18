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
            case CreateGameEvent e -> {
                session.sendMessage("game_created", e.game());
            }
            case AddPlayerEvent e -> {
                if (e.player() == session.player) {
                    session.sendMessage("current_player_joined");
                } else {
                    session.sendMessage("player_joined", e.player());
                }
            }
            case RemovePlayerEvent e -> {
                if (e.player() == session.player) {
                    session.sendMessage("current_player_left");
                } else {
                    session.sendMessage("player_left", e.player());
                }
            }
            case StartGameEvent ignored -> {
                session.sendMessage("game_started");
            }
            case FinishGameEvent e -> {
                if (e.result() == GameResult.CANCEL) {
                    session.sendMessage("game_canceled");
                } else {
                    session.sendMessage("game_finished", e.result());
                }
            }
            case StartTurnEvent e -> processStartTurnEvent(e);
            case PlayCardEvent e -> {
                session.sendMessage("player_played_card", e.player(), e.card());
            }
            case AddCardToTableEvent e -> {
                session.sendMessage("card_added_to_table", e.card());
            }
            case CreateFireworkEvent e -> {
                session.sendMessage("firework_created", e.card());
            }
            case AddRedTokenEvent e -> {
                session.sendMessage("red_token_added");
            }
            case DiscardCardEvent e -> {
                session.sendMessage("player_discarded_card", e.player(), e.card());
            }
            default -> {}
        }
    }

    void processStartTurnEvent(StartTurnEvent event) {
        var turnInfo = session.messages().getMessage(
                "turn_info",
                game.currentPlayer(),
                game.deckSize(),
                game.blueTokens(),
                game.redTokens(),
                buildCardTableText()
        );
        session.sendText(turnInfo, "MarkdownV2");
    }

    String buildCardTableText() {
        var table = new CardTable(session.player);
        game.players().forEach(table::addRow);
        table.addRow("table", game.tableCards());
        return table.buildText();
    }

    public void close() {
        subscription.remove();
    }

}
