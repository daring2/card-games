package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

public class StartGameCommand extends BaseCommand {

    public StartGameCommand(UserSession session) {
        super(session, "start_game");
    }

    @Override
    public void execute(CommandArguments arguments) {
        checkGameNotNull();
        game().start();
    }

    @Override
    public boolean isVisibleInMenu() {
        if (game() == null || game().isStarted())
            return false;
        var currentPlayers = game().players().size();
        var minPlayers = game().settings().getMinPlayers();
        return currentPlayers >= minPlayers;
    }

}
