package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

public class LaunchFireworksCommand extends BaseCommand {

    public LaunchFireworksCommand(UserSession session) {
        super(session, "launch_fireworks");
    }

    @Override
    public void execute(CommandArguments arguments) {
        session.game().launchFireworks(player());
    }

    @Override
    public boolean isVisibleInMenu() {
        if (!isCurrentPlayer())
            return false;
        return game().deckSize() == 0;
    }

}
