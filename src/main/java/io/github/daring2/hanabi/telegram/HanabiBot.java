package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HanabiBot extends TelegramLongPollingBot {

    final Context context;

    final Map<Long, BotSession> sessions = new ConcurrentHashMap<>();
    final Map<String, Game> games = new ConcurrentHashMap<>();

    public HanabiBot(Context context) {
        super(context.config.token);
        this.context = context;
    }

    @Override
    public String getBotUsername() {
        return "hanabi_pbot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        var message = update.getMessage();
        if (message == null)
            return;
        var session = sessions.computeIfAbsent(
                message.getChatId(),
                id -> createSession(message)
        );
        session.processUpdate(update);
    }

    BotSession createSession(Message message) {
        return new BotSession(
                this,
                message.getFrom(),
                message.getChatId()
        );
    }

    @ConfigurationProperties("hanabi-bot")
    public record Config(
            String token
    ) {
    }

    @Component
    public record Context(
            Config config,
            GameFactory gameFactory
    ) {
    }

}
