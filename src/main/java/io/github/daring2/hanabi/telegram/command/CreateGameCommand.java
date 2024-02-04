package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

public class CreateGameCommand extends BaseCommand {

    public CreateGameCommand(UserSession session) {
        super(session, "create_game");
    }

    @Override
    public void execute(CommandArguments arguments) {
        session.createGame();
    }

    @Override
    public boolean isVisibleInMenu() {
        return game() == null;
    }

}
