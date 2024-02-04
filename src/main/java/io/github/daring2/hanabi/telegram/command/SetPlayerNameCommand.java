package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SetPlayerNameCommand extends BaseCommand {

    public SetPlayerNameCommand(UserSession session) {
        super(session, "set_player_name");
    }

    @Override
    public void execute(CommandArguments arguments) {
        var playerName = arguments.get(1);
        if (isBlank(playerName)) {
            session.sendMessage("empty_player_name");
            return;
        }
        session.updatePlayerName(playerName);
    }

}
