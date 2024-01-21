package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameResult;
import io.github.daring2.hanabi.model.event.*;
import io.github.daring2.hanabi.model.event.GameEventBus.Subscription;

class GameEventProcessor implements AutoCloseable {

    final UserSession session;
    final Game game;
    final Subscription subscription;

    GameEventProcessor(UserSession session) {
        this.session = session;
        game = session.game;
        subscription = game.eventBus().subscribe(this::process);
    }

    void process(GameEvent event) {
        switch (event) {
            case CreateGameEvent e -> {
                sendMessage("game_created", e.game());
            }
            case AddPlayerEvent e -> {
                if (e.player() == session.player) {
                    sendMessage("current_player_joined");
                } else {
                    sendMessage("player_joined", e.player());
                }
            }
            case RemovePlayerEvent e -> {
                if (e.player() == session.player) {
                    sendMessage("current_player_left");
                } else {
                    sendMessage("player_left", e.player());
                }
            }
            case StartGameEvent ignored -> {
                sendMessage("game_started");
            }
            case StartTurnEvent e -> processStartTurnEvent(e);
            case PlayCardEvent e -> {
                sendMessage("player_played_card", e.player(), e.card());
            }
            case AddCardToTableEvent e -> {
                sendMessage("card_added_to_table", e.card());
            }
            case DeckEmptyEvent e -> {
                sendMessage("deck_is_empty");
            }
            case CreateFireworkEvent e -> {
                sendMessage("firework_created", e.card());
            }
            case AddRedTokenEvent e -> {
                sendMessage("red_token_added");
            }
            case SuggestEvent e -> {
                sendMessage("player_suggested_cards", e.player(), e.targetPlayer(), e.info());
            }
            case DiscardCardEvent e -> {
                sendMessage("player_discarded_card", e.player(), e.card());
            }
            case FinishGameEvent e -> processFinishEvent(e);
            default -> {}
        }
    }

    void processStartTurnEvent(StartTurnEvent event) {
        session.finishTurn();
        sendTurnInfo(true);
        if (game.currentPlayer() == session.player) {
            session.showActionKeyboard();
        }
    }

    void sendTurnInfo(boolean maskCards) {
        var turnInfo = session.messages().getMessage(
                "turn_info",
                game.currentPlayer(),
                game.deckSize(),
                game.blueTokens(),
                game.redTokens(),
                buildCardTableText(maskCards)
        );
        session.turnInfoMessage = session.sendText(
                turnInfo,
                "MarkdownV2"
        );
    }

    String buildCardTableText(boolean maskCards) {
        var table = new CardTable(session.player, maskCards);
        game.players().forEach(table::addRow);
        table.addRow("table", game.tableCards());
        return table.buildText();
    }

    void processFinishEvent(FinishGameEvent event) {
        session.finishTurn();
        sendTurnInfo(false);
        var result = event.result();
        if (result == GameResult.LAUNCH) {
            var scoreLevel = calculateScoreLevel(event.score());
            var reaction = session.messages().getMessage(
                    "firework_level" + scoreLevel
            );
            sendMessage("firework_launched", result, reaction);
        } else if (result == GameResult.LOSS) {
            sendMessage("game_lost", result);
        } else {
            sendMessage("game_canceled");
        }
    }

    int calculateScoreLevel(int score) {
        if (score >= Game.MAX_SCORE)
            return 5;
        return (score - 1) / 5;
    }

    void sendMessage(String code, Object... args) {
        session.sendMessage(code, args);
    }

    public void close() {
        subscription.remove();
    }

}
