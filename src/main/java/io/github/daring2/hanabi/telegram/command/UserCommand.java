package io.github.daring2.hanabi.telegram.command;

public interface UserCommand {

    String name();

    void execute(CommandArguments arguments);

    default boolean isVisibleInMenu() {
        return false;
    }

}
