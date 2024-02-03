package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameException;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import static io.github.daring2.hanabi.telegram.UserCommandUtils.parseCardInfo;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

class UserCommandProcessor {

    static final Logger logger = LoggerFactory.getLogger(UserSession.class);

    final UserSession session;
    final Update update;
    final Game game;
    final Player player;

    UserCommand command;
    ActionKeyboard keyboard;

    UserCommandProcessor(UserSession session, Update update) {
        this.session = session;
        this.update = update;
        this.game = session.game;
        this.player = session.player;
    }

    void process() {
        command = parseCommand();
        if (command .isEmpty())
            return;
        if (command.equals(session.activeCommand))
            return;
        if (game != null) {
            keyboard = session.createActionKeyboard(command);
        }
        try {
            processCommand();
        } catch (Exception e) {
            processCommandError(e);
        }
    }

    UserCommand parseCommand() {
        var text = "";
        var message = update.getMessage();
        var callback = update.getCallbackQuery();
        if (callback != null) {
            text = callback.getData();
        } else if (message != null) {
            text = message.getText();
        }
        return UserCommand.parse(text);
    }

    void processCommand() {
        //TODO handle start command
        switch (command.name()) {
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
        var gameId = command.getArgument(1);
        if (isNotBlank(gameId)) {
            session.joinGame(gameId);
        }
    }

    void processSetPlayerNameCommand() {
        var name =  command.getArgument(1);
        if (isBlank(name)) {
            sendMessage("empty_player_name");
            return;
        }
        session.updateUserName(name);
    }

    void processCreateGameCommand() {
        session.createGame();
    }

    void processJoinGameCommand() {
        var gameId = command.getArgument(1);
        session.joinGame(gameId);
    }

    void processLeaveGameCommand() {
        if (game == null)
            return;
        session.leaveCurrentGame();
    }

    void processStartGameCommand() {
        checkGameNotNull();
        game.start();
    }
    void processPlayCardCommand() {
        checkGameNotNull();
        if (command.argumentsCount() < 2) {
            keyboard.addCardSelectButtons();
            updateKeyboard();
            return;
        }
        var cardIndex = command.getIndexArgument(1);
        game.playCard(player, cardIndex);
    }

    void processDiscardCommand() {
        checkGameNotNull();
        if (command.argumentsCount() < 2) {
            keyboard.addCardSelectButtons();
            updateKeyboard();
            return;
        }
        var cardIndex = command.getIndexArgument(1);
        game.discardCard(player, cardIndex);
    }

    void processSuggestCommand() {
        checkGameNotNull();
        var argumentsCount = command.argumentsCount();
        if (argumentsCount < 3) {
            keyboard.addPlayerSelectButtons();
            if (argumentsCount == 2) {
                keyboard.addCardValueSelectButtons();
                keyboard.addColorSelectButtons();
            }
            updateKeyboard();
            return;
        }
        var playerIndex = command.getIndexArgument(1);
        var targetPlayer = session.getPlayer(playerIndex);
        var cardInfo = parseCardInfo(command.getArgument(2));
        game.suggest(player, targetPlayer, cardInfo);
    }

    void updateKeyboard() {
        session.activeCommand = command;
        keyboard.update(session.turnInfoMessage);
    }

    void processInvalidCommand() {
        sendMessage("invalid_command", command.name());
    }

    void processCommandError(Exception exception) {
        if (exception instanceof GameException e) {
            var errorText = messages().getMessage(
                    "errors." + e.getCode(),
                    e.getArguments()
            );
            sendMessage("game_error", errorText);
        } else {
            var commandText = command.buildText();
            logger.error("Cannot process command: " + commandText, exception);
            sendMessage("command_error", exception.getMessage());
        }
    }

    void checkGameNotNull() {
        if (game == null) {
            throw new GameException("game_is_null");
        }
    }

    void sendMessage(String code, Object... args) {
        var text = messages().getMessage(code, args);
        session.sendText(text);
    }

    GameMessages messages() {
        return session.messages();
    }

}
