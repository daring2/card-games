package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.GameException;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.telegram.command.CommandArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import static io.github.daring2.hanabi.telegram.command.CommandArguments.parseCommand;

class CommandProcessor {

    static final Logger logger = LoggerFactory.getLogger(UserSession.class);

    final UserSession session;

    Update update;
    CommandArguments commandArgs;

    CommandProcessor(UserSession session) {
        this.session = session;
    }

    void process(Update update) {
        this.update = update;
        buildCommandArgs();
        if (commandArgs.isEmpty())
            return;
        if (commandArgs.equals(session.commandArgs))
            return;
        session.commandArgs = commandArgs;
        try {
            processCommand();
        } catch (Exception e) {
            processCommandError(e);
        }
    }

    void buildCommandArgs() {
        var text = "";
        var message = update.getMessage();
        var callback = update.getCallbackQuery();
        if (callback != null) {
            text = callback.getData();
        } else if (message != null) {
            text = message.getText();
        }
        commandArgs = parseCommand(text);
    }

    void processCommand() {
        var command = session.commandRegistry.find(commandArgs);
        if (command != null) {
            command.execute(commandArgs);
        } else {
            processInvalidCommand();
        }
    }

    void processInvalidCommand() {
        session.sendMessage("invalid_command", commandArgs.name());
    }

    void processCommandError(Exception exception) {
        if (exception instanceof GameException e) {
            var errorText = messages().getMessage(
                    "errors." + e.getCode(),
                    e.getArguments()
            );
            session.sendMessage("game_error", errorText);
        } else {
            var commandText = commandArgs.buildText();
            logger.error("Cannot process command: " + commandText, exception);
            session.sendMessage("command_error", exception.getMessage());
        }
    }

    GameMessages messages() {
        return session.messages();
    }

}
