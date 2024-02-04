package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandRegistry {

    final UserSession session;
    final List<UserCommand> commands;
    final Map<String, UserCommand> commandsMap;

    public CommandRegistry(UserSession session) {
        this.session = session;
        commands = createCommands();
        commandsMap = buildCommandsMap();
    }

    List<UserCommand> createCommands() {
        return List.of(
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
    }

    Map<String, UserCommand> buildCommandsMap() {
        return commands.stream().collect(
                Collectors.toMap(UserCommand::name, Function.identity())
        );
    }

    public List<UserCommand> commands() {
        return commands;
    }

    public UserCommand find(CommandArguments args) {
        return commandsMap.get(args.name());
    }

}
