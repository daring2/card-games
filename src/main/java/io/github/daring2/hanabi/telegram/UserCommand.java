package io.github.daring2.hanabi.telegram;

import java.util.List;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.StringUtils.removeStart;

class UserCommand {

    final String name;
    final List<String> arguments;

    UserCommand(List<String> arguments) {
        this.name = parseName(arguments.getFirst());
        this.arguments = arguments;
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

    static String parseName(String argument) {
        return removeStart(argument.toLowerCase(), "/");
    }

}
