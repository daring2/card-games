package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.Player;
import io.github.daring2.hanabi.model.event.GameCreatedEvent;
import io.github.daring2.hanabi.model.event.GameEvent;
import io.github.daring2.hanabi.model.event.GameStartedEvent;
import io.github.daring2.hanabi.model.event.PlayerJoinedEvent;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class BotSession {

    final HanabiBot bot;
    final User user;
    final Long chatId;

    Game game;
    Player player;

    public BotSession(HanabiBot bot, User user, Long chatId) {
        this.bot = bot;
        this.user = user;
        this.chatId = chatId;
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
            case "/create" -> processCreateCommand();
            case "/join" -> processJoinCommand(command);
            case "/start" -> processStartCommand();
            default -> {
                sendMessage("invalid_command: %s", command.name);
            }
        }
    }

    void processCreateCommand() {
        createGame();
        registerGameListener();
        game.eventBus().publish(new GameCreatedEvent(game));
        createPlayer();
    }

    void processJoinCommand(UserCommand command) {
        //TODO leave current game
        var gameId = command.getArgument(1);
        game = bot.games.get(gameId);
        if (game == null) {
            sendMessage("invalid_game: %s", gameId);
            return;
        }
        registerGameListener();
        createPlayer();

    }

    void processStartCommand() {
        if (game == null) {
            sendMessage("game_is_null");
            return;
        }
        game.start();
    }

    void createGame() {
        //TODO leave current game
        game = bot.context.gameFactory().create();
        bot.games.put(game.id(), game);
    }

    void registerGameListener() {
        game.eventBus().subscribe(this::processGameEvent);
    }

    void createPlayer() {
        player = new Player(user.getUserName());
        game.addPlayer(player);
    }

    void processGameEvent(GameEvent event) {
        //TODO introduce GameEventProcessor
        switch (event) {
            case GameCreatedEvent e -> {
                sendMessage("game_created: %s", game.id());
            }
            case PlayerJoinedEvent e -> {
                sendMessage("player_joined: game=%s, player=%s", game.id(), player);
            }
            case GameStartedEvent e -> {
                sendMessage("game_started: %s", game.id());
            }
            default -> {
            }
        }
    }

    void sendMessage(String format, Object... args) {
        var text = String.format(format, args);
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

}
