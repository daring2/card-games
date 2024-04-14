package io.github.daring2.hanabi.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
class BotStateManager {

    static final Logger logger = LoggerFactory.getLogger(UserSession.class);

    final Context context;
    final File stateFile;
    final ObjectMapper jsonMapper;

    BotStateManager(Context context) {
        this.context = context;
        stateFile = context.config.stateFile;
        jsonMapper = createJsonMapper();
    }

    void loadState(HanabiBot bot) {
        logger.info("Load HanabiBot state");
        if (!stateFile.exists())
            return;
        try {
            var state = jsonMapper.readValue(stateFile, HanabiBot.State.class);
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
        var games = bot.games.values().stream()
                .filter(it -> !it.isFinished())
                .toList();
        var sessions = bot.sessions.values().stream()
                .map(UserSession::createState)
                .toList();
        var state = new HanabiBot.State(games, sessions);
        try {
            jsonMapper.writeValue(stateFile, state);
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
            File stateFile
    ) {
    }

    @Component
    public record Context(
            Config config
    ) {
    }

}
