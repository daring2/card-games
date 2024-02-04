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
            case CreateGameEvent e -> processCreateGameEvent(e);
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
            case PlayCardEvent e -> processPlayCardEvent(e);
//            case AddCardToTableEvent e -> {}
//            case AddRedTokenEvent e -> {}
            case DeckEmptyEvent e -> {
                sendMessage("deck_is_empty");
            }
            case CreateFireworkEvent e -> {
                sendMessage("firework_created", e.card());
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

    void processCreateGameEvent(CreateGameEvent event) {
        var link = "https://t.me/hanabi_pbot" +
                "?start=" + event.game().id();
        sendMessage("game_created", link);
    }

    void processStartTurnEvent(StartTurnEvent event) {
        session.finishTurn();
        sendTurnInfo(true);
        if (game.currentPlayer() == session.player) {
            session.showKeyboard();
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

    void processPlayCardEvent(PlayCardEvent event) {
        var card = event.card();
        var text = getMessage("player_played_card", event.player(), card);
        text += "\n";
        if (event.isValid()) {
            text += getMessage("card_added_to_table", card);
        } else {
            text += getMessage("red_token_added");
        }
        session.sendText(text);
    }

    void processFinishEvent(FinishGameEvent event) {
        session.finishTurn();
        sendTurnInfo(false);
        var result = event.result();
        if (result == GameResult.LAUNCH) {
            var score = event.score();
            var scoreLevel = calculateScoreLevel(score);
            var reaction = session.messages().getMessage(
                    "firework_level" + scoreLevel
            );
            sendMessage("firework_launched", result, score, reaction);
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

    String getMessage(String code, Object... args) {
        return session.messages().getMessage(code, args);
    }

    public void close() {
        subscription.remove();
    }

}
