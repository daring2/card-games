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
        if (command == null)
            return;
        keyboard = session.createActionKeyboard(command);
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
        switch (command.name) {
            case "set_player_name" -> processSetPlayerNameCommand();
            case "create" -> processCreateCommand();
            case "join" -> processJoinCommand();
            case "leave" -> processLeaveCommand();
            case "start" -> processStartCommand();
            case "play_card", "p" -> processPlayCardCommand();
            case "discard", "d" -> processDiscardCommand();
            case "suggest", "s" -> processSuggestCommand();
            default -> processInvalidCommand();
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

    void processCreateCommand() {
        session.createGame();
    }

    void processJoinCommand() {
        var gameId = command.getArgument(1);
        session.joinGame(gameId);
    }

    void processLeaveCommand() {
        if (game == null)
            return;
        session.leaveCurrentGame();
    }

    void processStartCommand() {
        checkGameNotNull();
        game.start();
    }
    void processPlayCardCommand() {
        checkGameNotNull();
        if (command.getArgumentCount() < 2) {
            keyboard.addCardSelectButtons();
            openKeyboard();
            return;
        }
        var cardIndex = command.getIndexArgument(1);
        game.playCard(player, cardIndex);
        closeKeyboard();
    }

    void processDiscardCommand() {
        checkGameNotNull();
        if (command.getArgumentCount() < 2) {
            keyboard.addCardSelectButtons();
            openKeyboard();
            return;
        }
        var cardIndex = command.getIndexArgument(1);
        game.discardCard(player, cardIndex);
        closeKeyboard();
    }

    void processSuggestCommand() {
        checkGameNotNull();
        if (command.getArgumentCount() < 3) {
            //TODO show inline keyboard
            return;
        }
        var playerIndex = command.getIndexArgument(1);
        var targetPlayer = session.getPlayer(playerIndex);
        var cardInfo = parseCardInfo(command.getArgument(2));
        game.suggest(player, targetPlayer, cardInfo);
        closeKeyboard();
    }

    void openKeyboard() {
        var callback = update.getCallbackQuery();
        if (callback != null) {
            keyboard.update(callback.getMessage());
        } else {
            keyboard.open();
        }
    }

    void closeKeyboard() {
        var callback = update.getCallbackQuery();
        if (callback != null) {
            keyboard.close(callback.getMessage());
        }
    }

    void processInvalidCommand() {
        sendMessage("invalid_command", command.name);
    }

    void processCommandError(Exception exception) {
        if (exception instanceof GameException e) {
            var errorText = messages().getMessage(
                    "errors." + e.getCode(),
                    e.getArguments()
            );
            sendMessage("game_error", errorText);
        } else {
            logger.error("Cannot process command: " + command.expression, exception);
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
