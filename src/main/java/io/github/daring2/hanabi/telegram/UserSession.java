package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.model.Player;
import io.github.daring2.hanabi.model.event.GameCreatedEvent;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class UserSession {

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
            processCommand(command);
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
            case "/set_user_name" -> processSetUserNameCommand(command);
            case "/create" -> processCreateCommand();
            case "/join" -> processJoinCommand(command);
            case "/leave" -> processLeaveCommand();
            case "/start" -> processStartCommand();
            default -> processInvalidCommand(command);
        }
    }

    void processSetUserNameCommand(UserCommand command) {
        var name =  command.getArgument(1);
        if (isBlank(name)) {
            sendMessage("empty_user_name");
            return;
        }
        userName = name;
        sendMessage("user_name_updated", userName);
    }

    void processCreateCommand() {
        createGame();
        registerGameListener();
        game.eventBus().publish(new GameCreatedEvent(game));
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
        if (game == null) {
            sendMessage("game_is_null");
            return;
        }
        game.start();
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
        var text = gameMessages().getMessage(code, args);
        sendText(text);
    }

    void sendText(String text) {
        try {
            var sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build();
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    GameMessages gameMessages() {
        return bot.context.gameMessages();
    }

}
