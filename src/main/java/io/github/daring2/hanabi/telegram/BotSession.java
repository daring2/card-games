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

    public synchronized void processUpdate(Update update) {
        var message = update.getMessage();
        var command = parseCommand(message);
        if (command == null)
            return;
        if ("/create".equals(command.name)) {
            processCreateCommand();
        } else if ("/join".equals(command.name)) {
            processJoinCommand(command);
        } else {
            sendMessage("Invalid command: %s", message.getText());
        }
    }

    void processCreateCommand() {
        game = bot.context.gameFactory().create();
        bot.games.put(game.getId(), game);
        player = createPlayer();
        game.addPlayer(player);
        sendMessage("game_created: %s", game.getId());
    }

    void processJoinCommand(UserCommand command) {
        var gameId = command.getArgument(1);
        game = bot.games.get(gameId);
        if (game == null) {
            sendMessage("invalid_game: %s", gameId);
            return;
        }
        player = createPlayer();
        game.addPlayer(player);
        sendMessage("player_joined: game=%s, player=%s", game.getId(), player);
    }

    Player createPlayer() {
        return new Player(user.getUserName());
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
