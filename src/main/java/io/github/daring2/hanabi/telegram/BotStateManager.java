package io.github.daring2.hanabi.telegram;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
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


    void saveState(HanabiBot bot) {
        logger.info("Save HanabiBot state");
        var games = bot.games.values().stream()
                .filter(it -> !it.isFinished())
                .toList();
        var state = new BotState(games);
        try {
            jsonMapper.writeValue(stateFile, state);
        } catch (IOException e) {
            logger.warn("Cannot save HanabiBot state", e);
        }
    }

    ObjectMapper createJsonMapper() {
        var mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
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
