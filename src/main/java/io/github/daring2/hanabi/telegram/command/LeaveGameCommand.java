package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

public class LeaveGameCommand extends BaseCommand {

    public LeaveGameCommand(UserSession session) {
        super(session, "leave_game");
    }

    @Override
    public void execute(CommandArguments arguments) {
        session.leaveCurrentGame();
    }

    @Override
    public boolean isVisibleInMenu() {
        return session.game() != null;
    }

}
