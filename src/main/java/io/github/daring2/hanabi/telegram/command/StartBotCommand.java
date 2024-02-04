package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class StartBotCommand extends BaseCommand {

    public StartBotCommand(UserSession session) {
        super(session, "start");
    }

    @Override
    public void execute(CommandArguments arguments) {
        var gameId = arguments.get(1);
        if (isNotBlank(gameId)) {
            session.joinGame(gameId);
        }
    }

}
