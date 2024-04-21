package io.github.daring2.hanabi.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class BotStateManager {

    static final Logger logger = LoggerFactory.getLogger(UserSession.class);

    final Context context;
    final WritableResource stateFile;
    final ObjectMapper jsonMapper;

    BotStateManager(Context context) {
        this.context = context;
        stateFile = context.config.stateFile;
        jsonMapper = createJsonMapper();
    }

    void loadState(HanabiBot bot) {
        logger.info("Load HanabiBot state");
        if (stateFile == null || !stateFile.exists())
            return;
        try (var stream = stateFile.getInputStream()){
            var state = jsonMapper.readValue(stream, HanabiBot.State.class);
            for (var game : state.games()) {
                bot.games.put(game.id(), game);
            }
            state.sessions().forEach(it -> loadSessionState(bot, it));
        } catch (IOException e) {
            logger.warn("Cannot load HanabiBot state", e);
        }
    }

    void loadSessionState(HanabiBot bot, UserSession.State state) {
        var session = new UserSession(bot, state);
        bot.sessions.put(session.chatId, session);
    }

    void saveState(HanabiBot bot) {
        logger.info("Save HanabiBot state");
        if (stateFile == null)
            return;
        var games = bot.games.values().stream()
                .filter(it -> !it.isFinished())
                .toList();
        var sessions = bot.sessions.values().stream()
                .map(UserSession::createState)
                .toList();
        var state = new HanabiBot.State(games, sessions);
        try (var stream = stateFile.getOutputStream()){
            jsonMapper.writeValue(stream, state);
        } catch (IOException e) {
            logger.warn("Cannot save HanabiBot state", e);
        }
    }

    ObjectMapper createJsonMapper() {
        var mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    @ConfigurationProperties("hanabi-bot.state-manager")
    public record Config(
            WritableResource stateFile
    ) {
    }

    @Component
    public record Context(
            Config config,
            ResourceLoader resourceLoader
    ) {
    }

}
