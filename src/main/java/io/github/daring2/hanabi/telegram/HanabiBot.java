package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameFactory;
import io.github.daring2.hanabi.model.GameMessages;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("!test")
public class HanabiBot extends TelegramLongPollingBot {

    final Context context;

    final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();
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
        processMessage(update);
        processCallbackQuery(update);
    }

    void processMessage(Update update) {
        var message = update.getMessage();
        if (message == null)
            return;
        var session = sessions.computeIfAbsent(
                message.getChatId(),
                id -> createUserSession(message)
        );
        session.processUpdate(update);
    }

    void processCallbackQuery(Update update) {
        var query = update.getCallbackQuery();
        if (query == null)
            return;
        var chatId = query.getMessage().getChatId();
        var session = sessions.get(chatId);
        if (session != null) {
            session.processUpdate(update);
        }
    }

    UserSession createUserSession(Message message) {
        return new UserSession(
                this,
                message.getFrom(),
                message.getChatId()
        );
    }

    void executeSync(BotApiMethod<?> method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @ConfigurationProperties("hanabi-bot")
    public record Config(
            String token
    ) {
    }

    @Component
    public record Context(
            Config config,
            GameFactory gameFactory,
            GameMessages gameMessages
    ) {
    }

}
