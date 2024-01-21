package io.github.daring2.hanabi.telegram;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.*;

class UserCommand {

    final String name;
    final List<String> arguments;

    UserCommand(List<String> arguments) {
        this.name = parseName(arguments.getFirst());
        this.arguments = arguments;
    }

    boolean isEmpty() {
        return arguments.isEmpty();
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
            return new UserCommand(List.of());
        var arguments = Arrays.stream(text.split(" "))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
        return new UserCommand(arguments);
    }

    static String parseName(String argument) {
        return removeStart(argument.toLowerCase(), "/");
    }

}
