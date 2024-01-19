package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.*;
import io.github.daring2.hanabi.model.event.CreateGameEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

import static io.github.daring2.hanabi.model.Game.MAX_CARD_VALUE;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class UserSession {

    static final Logger logger = LoggerFactory.getLogger(UserSession.class);

    final HanabiBot bot;
    final User user;
    final Long chatId;

    String userName;
    Game game;
    Player player;
    GameEventProcessor eventProcessor;

    public UserSession(HanabiBot bot, User user, Long chatId) {
        this.bot = bot;
        this.user = user;
        this.chatId = chatId;
        this.userName = user.getUserName();
    }

    public void processUpdate(Update update) {
        var lock = game != null ? game : this; //TODO refactor
        synchronized (lock) {
            var message = update.getMessage();
            var command = parseCommand(message);
            if (command == null)
                return;
            try {
                processCommand(command);
            } catch (Exception e) {
                processCommandError(message, e);
            }
        }
    }

    UserCommand parseCommand(Message message) {
        var text = message.getText();
        if (isBlank(text))
            return null;
        var arguments = Arrays.stream(text.split(" "))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
        return new UserCommand(arguments);
    }

    void processCommand(UserCommand command) {
        if (command == null)
            return;
        switch (command.name) {
            case "set_player_name" -> processSetPlayerNameCommand(command);
            case "create" -> processCreateCommand();
            case "join" -> processJoinCommand(command);
            case "leave" -> processLeaveCommand();
            case "start" -> processStartCommand();
            case "play_card", "p" -> processPlayCardCommand(command);
            case "suggest", "s" -> processSuggestCommand(command);
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
        userName = name;
        sendMessage("player_name_updated", userName);
    }

    void processCreateCommand() {
        createGame();
        registerGameListener();
        game.eventBus().publish(new CreateGameEvent(game));
        createPlayer();
    }

    void processJoinCommand(UserCommand command) {
        leaveCurrentGame();
        var gameId = command.getArgument(1);
        game = bot.games.get(gameId);
        if (game == null) {
            sendMessage("game_not_found", gameId);
            return;
        }
        registerGameListener();
        createPlayer();
    }

    void processLeaveCommand() {
        if (game == null)
            return;
        leaveCurrentGame();
    }

    void processStartCommand() {
        checkGameNotNull();
        game.start();
    }

    void processPlayCardCommand(UserCommand command) {
        checkGameNotNull();
        var cardIndex = command.getIndexArgument(1);
        game.playCard(player, cardIndex);
    }

    void processSuggestCommand(UserCommand command) {
        checkGameNotNull();
        var targetPlayer = getPlayer(command.getIndexArgument(1));
        var cardInfo = parseCardInfo(command.getArgument(2));
        game.suggest(player, targetPlayer, cardInfo);
    }

    void processDiscardCommand(UserCommand command) {
        checkGameNotNull();
        var cardIndex = command.getIndexArgument(1);
        game.discardCard(player, cardIndex);
    }

    void processInvalidCommand(UserCommand command) {
        sendMessage("invalid_command", command.name);
    }

    void createGame() {
        leaveCurrentGame();
        game = bot.context.gameFactory().create();
        bot.games.put(game.id(), game);
    }

    void registerGameListener() {
        eventProcessor = new GameEventProcessor(this);
    }

    void createPlayer() {
        player = new Player(userName);
        game.addPlayer(player);
    }

    void leaveCurrentGame() {
        if (game == null)
            return;
        game.removePlayer(player);
        eventProcessor.close();
        game = null;
    }

    void sendMessage(String code, Object... args) {
        var text = messages().getMessage(code, args);
        sendText(text);
    }

    void sendText(String text, String parseMode) {
        try {
            var sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode(parseMode)
                    .build();
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    void sendText(String text) {
        sendText(text, null);
    }

    void processCommandError(Message message, Exception exception) {
        if (exception instanceof GameException e) {
            var errorText = messages().getMessage(
                    "errors." + e.getCode(),
                    e.getArguments()
            );
            sendMessage("game_error", errorText);
        } else {
            logger.error("Cannot process command: " + message.getText(), exception);
            sendMessage("command_error", exception.getMessage());
        }
    }

    void checkGameNotNull() {
        if (game == null) {
            throw new GameException("game_is_null");
        }
    }

    Player getPlayer(int index) {
        var players = game.players();
        if (index < 0 || index >= players.size()) {
            throw new GameException("invalid_player_index");
        }
        return players.get(index);
    }

    CardInfo parseCardInfo(String infoExp) {
        if (isNumeric(infoExp)) {
            var value = parseInt(infoExp);
            if (value >= 1 && value <= MAX_CARD_VALUE)
                return new CardInfo(value);
        } else {
            for (Color color : Color.valueList) {
                if (color.shortName.equals(infoExp))
                    return new CardInfo(color);
            }
        }
        throw new GameException("invalid_suggestion");
    }

    GameMessages messages() {
        return bot.context.gameMessages();
    }

}
