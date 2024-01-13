package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class BotSession {

    final HanabiBot bot;
    final Long chatId;

    Game game;

    public BotSession(HanabiBot bot, Long chatId) {
        this.bot = bot;
        this.chatId = chatId;
    }

    public synchronized void processUpdate(Update update) {
        var message = update.getMessage();
        var arguments = parseCommandArguments(message);
        if (arguments.isEmpty())
            return;
        var command = arguments.getFirst().toLowerCase();
        if ("/create".equals(command)) {
            game = bot.context.gameFactory().create();
            bot.games.put(game.getId(), game);
            sendText("game_created: id=" + game.getId());
        } else if ("/join".equals(command) && arguments.size() == 2) {
            //TODO implement
        } else {
            sendText("Invalid command: " + message.getText());
        }
    }

    List<String> parseCommandArguments(Message message) {
        var text = message.getText();
        if (isBlank(text))
            return List.of();
        return Arrays.stream(text.split(" "))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
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
