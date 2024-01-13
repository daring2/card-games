package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.Player;
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
        synchronized (game) {
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
        if ("/create".equals(command.name)) {
            processCreateCommand();
        } else if ("/join".equals(command.name)) {
            processJoinCommand(command);
        } else {
            sendMessage("Invalid command: %s", command.name);
        }
    }

    void processCreateCommand() {
        createGame();
        createPlayer();
        game.addPlayer(player);
        sendMessage("game_created: %s", game.id());
    }

    void createGame() {
        game = bot.context.gameFactory().create();
        bot.games.put(game.id(), game);
    }

    void processJoinCommand(UserCommand command) {
        var gameId = command.getArgument(1);
        game = bot.games.get(gameId);
        if (game == null) {
            sendMessage("invalid_game: %s", gameId);
            return;
        }
        //TODO check if player is already joined
        createPlayer();
        game.addPlayer(player);
        sendMessage("player_joined: game=%s, player=%s", game.id(), player);
    }

    void createPlayer() {
        player = new Player(user.getUserName());
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