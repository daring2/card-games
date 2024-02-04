package io.github.daring2.hanabi.telegram.command;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.*;

public record CommandArguments(
        List<String> arguments
) {

    public int size() {
        return arguments.size();
    }

    public boolean isEmpty() {
        return EMPTY.equals(this);
    }

    public String name() {
        return get(0);
    }

    public String get(int index) {
        if (index >= arguments.size())
            return null;
        return arguments.get(index);
    }

    public int getIndexValue(int index) {
        var value = get(index);
        if (!isNumeric(value))
            return -1;
        return parseInt(value) - 1;
    }

    public String buildText() {
        return join(" ", arguments);
    }

    public static CommandArguments parseCommand(String text) {
        if (isBlank(text))
            return EMPTY;
        var arguments = Arrays.stream(text.split(" "))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        var name = arguments.getFirst();
        name = removeStart(name.toLowerCase(), "/");
        arguments.set(0, name);
        return new CommandArguments(arguments);
    }

    public static final CommandArguments EMPTY = empty();

    public static CommandArguments empty() {
        return new CommandArguments(List.of());
    }

}
