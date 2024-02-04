package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandRegistry {

    final UserSession session;
    final Map<String, UserCommand> commands;

    public CommandRegistry(UserSession session) {
        this.session = session;
        commands = createCommands();
    }

    Map<String, UserCommand> createCommands() {
        var commands = List.of(
                new StartBotCommand(session),
                new SetPlayerNameCommand(session),
                new CreateGameCommand(session),
                new JoinGameCommand(session),
                new LeaveGameCommand(session),
                new StartGameCommand(session),
                new PlayCardCommand(session),
                new DiscardCommand(session),
                new SuggestCommand(session)
        );
        return commands.stream().collect(
                Collectors.toMap(UserCommand::name, Function.identity())
        );
    }

    public UserCommand find(CommandArguments commandArgs) {
        return commands.get(commandArgs.name());
    }

}
