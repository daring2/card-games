package io.github.daring2.hanabi.telegram;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNumeric;

class UserCommand {

    final String name;
    final List<String> arguments;

    UserCommand(List<String> arguments) {
        this.name = arguments.getFirst().toLowerCase();
        this.arguments = arguments;
    }

    public String getArgument(int index) {
        if (index >= arguments.size())
            return null;
        return arguments.get(index);
    }

    public int getIndexArgument(int index) {
        var value = getArgument(index);
        if (!isNumeric(value))
            return -1;
        return Integer.parseInt(value);
    }

}
