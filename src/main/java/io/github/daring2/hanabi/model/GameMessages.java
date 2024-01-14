package io.github.daring2.hanabi.model;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class GameMessages {

    final Context context;

    public GameMessages(Context context) {
        this.context = context;
    }

    public String getMessage(String code, Object... args) {
        args = Arrays.stream(args)
                .map(this::formatArgument)
                .toArray();
        var message = context.messageSource.getMessage(
                "hanabi." + code,
                args, "",
                Locale.getDefault()
        );
        if (isEmpty(message)) {
            message = code + ": " + Arrays.toString(args);
        }
        return message;
    }

    Object formatArgument(Object argument) {
        if (argument == null)
            return null;
        return switch (argument) {
            case Game it -> it.id();
            case GameResult it -> getResultLabel(it);
            case Player it -> it.name();
            default -> argument;
        };
    }

    String getResultLabel(GameResult result) {
        return getMessage("game_result." + result.name());
    }

    @Component
    public record Context(
            MessageSource messageSource
    ) {
    }

}
