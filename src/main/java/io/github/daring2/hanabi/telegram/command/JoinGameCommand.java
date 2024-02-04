package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

public class JoinGameCommand extends BaseCommand  {

    public JoinGameCommand(UserSession session) {
        super(session, "join_game");
    }

    @Override
    public void execute(CommandArguments arguments) {
        var gameId = arguments.get(1);
        session.joinGame(gameId);
    }

}
