package io.github.daring2.hanabi.telegram;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.*;

class UserCommand {

    final String name;
    final List<String> arguments;
    final String expression; //TODO remove

    UserCommand(String name, List<String> arguments) {
        this.name = name;
        this.arguments = arguments;
        this.expression = name + " " + join(" ", arguments);
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
        var arguments = Arrays.stream(text.split(" "))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        var name = removeStart(arguments.removeFirst(), "/");
        return new UserCommand(name, arguments);
    }

    static UserCommand empty() {
        return new UserCommand("", List.of());
    }

}
