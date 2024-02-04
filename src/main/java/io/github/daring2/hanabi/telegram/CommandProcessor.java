package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameException;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.model.Player;
import io.github.daring2.hanabi.telegram.command.CommandArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import static io.github.daring2.hanabi.telegram.command.CommandArguments.parseCommand;
import static io.github.daring2.hanabi.telegram.command.UserCommandUtils.parseCardInfo;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

class CommandProcessor {

    static final Logger logger = LoggerFactory.getLogger(UserSession.class);

    final UserSession session;

    Update update;
    CommandArguments activeCommand;

    CommandProcessor(UserSession session) {
        this.session = session;
    }

    void process(Update update) {
        this.update = update;
        buildCommandArgs();
        if (commandArgs().isEmpty())
            return;
        if (commandArgs().equals(activeCommand))
            return;
        if (game() != null) {
            keyboard().reset();
            keyboard().addActionButtons();
        }
        try {
            processCommand();
        } catch (Exception e) {
            processCommandError(e);
        }
    }

    void buildCommandArgs() {
        var text = "";
        var message = update.getMessage();
        var callback = update.getCallbackQuery();
        if (callback != null) {
            text = callback.getData();
        } else if (message != null) {
            text = message.getText();
        }
        session.commandArgs = parseCommand(text);
    }

    void processCommand() {
        //TODO handle start command
        switch (commandArgs().name()) {
            case "start" -> processStartCommand();
            case "set_player_name" -> processSetPlayerNameCommand();
            case "create_game" -> processCreateGameCommand();
            case "join_game" -> processJoinGameCommand();
            case "leave_game" -> processLeaveGameCommand();
            case "start_game" -> processStartGameCommand();
            case "play_card", "p" -> processPlayCardCommand();
            case "discard", "d" -> processDiscardCommand();
            case "suggest", "s" -> processSuggestCommand();
            default -> processInvalidCommand();
        }
    }

    void processStartCommand() {
        var gameId = commandArgs().get(1);
        if (isNotBlank(gameId)) {
            session.joinGame(gameId);
        }
    }

    void processSetPlayerNameCommand() {
        var playerName = commandArgs().get(1);
        if (isBlank(playerName)) {
            sendMessage("empty_player_name");
            return;
        }
        session.updatePlayerName(playerName);
    }

    void processCreateGameCommand() {
        session.createGame();
    }

    void processJoinGameCommand() {
        var gameId = commandArgs().get(1);
        session.joinGame(gameId);
    }

    void processLeaveGameCommand() {
        if (game() == null)
            return;
        session.leaveCurrentGame();
    }

    void processStartGameCommand() {
        checkGameNotNull();
        game().start();
    }
    void processPlayCardCommand() {
        checkGameNotNull();
        if (commandArgs().size() < 2) {
            keyboard().addCardSelectButtons();
            updateKeyboard();
            return;
        }
        var cardIndex = commandArgs().getIndexValue(1);
        game().playCard(player(), cardIndex);
    }

    void processDiscardCommand() {
        checkGameNotNull();
        if (commandArgs().size() < 2) {
            keyboard().addCardSelectButtons();
            updateKeyboard();
            return;
        }
        var cardIndex = commandArgs().getIndexValue(1);
        game().discardCard(player(), cardIndex);
    }

    void processSuggestCommand() {
        checkGameNotNull();
        var argumentsCount = commandArgs().size();
        if (argumentsCount < 3) {
            keyboard().addPlayerSelectButtons();
            if (argumentsCount == 2) {
                keyboard().addCardValueSelectButtons();
                keyboard().addColorSelectButtons();
            }
            updateKeyboard();
            return;
        }
        var playerIndex = commandArgs().getIndexValue(1);
        var targetPlayer = session.getPlayer(playerIndex);
        var cardInfo = parseCardInfo(commandArgs().get(2));
        game().suggest(player(), targetPlayer, cardInfo);
    }

    void updateKeyboard() {
        activeCommand = commandArgs();
        session.updateKeyboard();
    }

    void processInvalidCommand() {
        sendMessage("invalid_command", commandArgs().name());
    }

    void processCommandError(Exception exception) {
        if (exception instanceof GameException e) {
            var errorText = messages().getMessage(
                    "errors." + e.getCode(),
                    e.getArguments()
            );
            sendMessage("game_error", errorText);
        } else {
            var commandText = commandArgs().buildText();
            logger.error("Cannot process command: " + commandText, exception);
            sendMessage("command_error", exception.getMessage());
        }
    }

    void checkGameNotNull() {
        if (game() == null) {
            throw new GameException("game_is_null");
        }
    }

    void sendMessage(String code, Object... args) {
        var text = messages().getMessage(code, args);
        session.sendText(text);
    }

    Game game() {
        return session.game;
    }

    Player player() {
        return session.player;
    }

    CommandArguments commandArgs() {
        return session.commandArgs;
    }

    ActionKeyboard keyboard() {
        return session.keyboard;
    }

    GameMessages messages() {
        return session.messages();
    }

}
