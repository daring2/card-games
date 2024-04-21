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

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
class BotStateManager {

    static final Logger logger = LoggerFactory.getLogger(UserSession.class);

    final Context context;
    final ObjectMapper jsonMapper;

    BotStateManager(Context context) {
        this.context = context;
        jsonMapper = createJsonMapper();
    }

    void loadState(HanabiBot bot) {
        logger.info("Load HanabiBot state");
        var stateFile = getStateFile();
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
        var stateFile = getStateFile();
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

    WritableResource getStateFile() {
        var url = context.config.stateFile;
        if (isBlank(url))
            return null;
        return (WritableResource) context.resourceLoader.getResource(url);
    }

    @ConfigurationProperties("hanabi-bot.state-manager")
    public record Config(
            String stateFile
    ) {
    }

    @Component
    public record Context(
            Config config,
            ResourceLoader resourceLoader
    ) {
    }

}
