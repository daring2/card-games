package io.github.daring2.hanabi.telegram;

import java.util.List;

class UserCommand {

    final String name;
    final List<String> arguments;

    UserCommand(List<String> arguments) {
        this.name = arguments.getFirst();
        this.arguments = arguments;
    }

    public String getArgument(int index) {
        if (index >= arguments.size())
            return null;
        return arguments.get(index);
    }

}
