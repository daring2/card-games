package io.github.daring2.hanabi.telegram;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.*;

class UserCommand {

    final String name;
    final List<String> arguments;
    final String expression; //TODO remove

    UserCommand(List<String> arguments) {
        this.name = arguments.getFirst();
        this.arguments = arguments;
        this.expression = String.join(" ", arguments);
    }

    String getArgument(int index) {
        if (index >= arguments.size())
            return null;
        return arguments.get(index);
    }

    int getIndexArgument(int index) {
        var value = getArgument(index);
        if (!isNumeric(value))
            return -1;
        return parseInt(value) - 1;
    }

    static UserCommand parse(String text) {
        if (isBlank(text))
            return null;
        text = removeStart(text.toLowerCase(), "/");
        var arguments = Arrays.stream(text.split(" "))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(StringUtils::isNotBlank)
                .toList();
        return new UserCommand(arguments);
    }

    static UserCommand empty() {
        return new UserCommand(List.of(""));
    }

}
