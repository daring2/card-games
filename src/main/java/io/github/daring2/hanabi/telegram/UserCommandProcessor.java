package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameException;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.model.Player;
import org.apache.logging.log4j.util.Strings;
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

    UserCommandProcessor(UserSession session, Update update) {
        this.session = session;
        this.update = update;
        this.game = session.game;
        this.player = session.player;
    }

    void process() {
        var command = parseCommand();
        if (command == null)
            return;
        try {
            processCommand(command);
        } catch (Exception e) {
            processCommandError(command, e);
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

    void processCommand(UserCommand command) {
        switch (command.name) {
            case "set_player_name" -> processSetPlayerNameCommand(command);
            case "create" -> processCreateCommand();
            case "join" -> processJoinCommand(command);
            case "leave" -> processLeaveCommand();
            case "start" -> processStartCommand();
            case "suggest", "s" -> processSuggestCommand(command);
            case "play_card", "p" -> processPlayCardCommand(command);
            case "discard", "d" -> processDiscardCommand(command);
            default -> processInvalidCommand(command);
        }
    }

    void processSetPlayerNameCommand(UserCommand command) {
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

    void processJoinCommand(UserCommand command) {
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
    void processPlayCardCommand(UserCommand command) {
        checkGameNotNull();
        if (command.getArgumentCount() < 2) {
            //TODO show inline keyboard
            return;
        }
        var cardIndex = command.getIndexArgument(1);
        game.playCard(player, cardIndex);
    }

    void processDiscardCommand(UserCommand command) {
        checkGameNotNull();
        if (command.getArgumentCount() < 2) {
            //TODO show inline keyboard
            return;
        }
        var cardIndex = command.getIndexArgument(1);
        game.discardCard(player, cardIndex);
    }

    void processSuggestCommand(UserCommand command) {
        checkGameNotNull();
        if (command.getArgumentCount() < 3) {
            //TODO show inline keyboard
            return;
        }
        var playerIndex = command.getIndexArgument(1);
        var targetPlayer = session.getPlayer(playerIndex);
        var cardInfo = parseCardInfo(command.getArgument(2));
        game.suggest(player, targetPlayer, cardInfo);
    }

    void processInvalidCommand(UserCommand command) {
        sendMessage("invalid_command", command.name);
    }

    void processCommandError(UserCommand command, Exception exception) {
        if (exception instanceof GameException e) {
            var errorText = messages().getMessage(
                    "errors." + e.getCode(),
                    e.getArguments()
            );
            sendMessage("game_error", errorText);
        } else {
            var text = Strings.join(command.arguments, ' ');
            logger.error("Cannot process command: " + text, exception);
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
