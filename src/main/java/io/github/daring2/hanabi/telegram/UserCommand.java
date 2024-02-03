package io.github.daring2.hanabi.telegram;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.*;

record UserCommand(
        List<String> arguments
) {

    String name() {
        return getArgument(0);
    }

    int argumentsCount() {
        return arguments.size();
    }

    boolean isEmpty() {
        return EMPTY.equals(this);
    }

    String buildText() {
        return join(" ", arguments);
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
            return EMPTY;
        var arguments = Arrays.stream(text.split(" "))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        var name = arguments.getFirst();
        name = removeStart(name.toLowerCase(), "/");
        arguments.set(0, name);
        return new UserCommand(arguments);
    }

    static final UserCommand EMPTY = empty();

    static UserCommand empty() {
        return new UserCommand(List.of());
    }

}
