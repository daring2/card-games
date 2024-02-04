package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.telegram.command.CommandArguments;

import java.util.ArrayList;

import static io.github.daring2.hanabi.model.Game.MAX_CARD_VALUE;

public class ActionKeyboard {

    //TODO refactor, remove?

    final UserSession session;
    final ActionMenu menu;

    ActionKeyboard(UserSession session) {
        this.session = session;
        this.menu = session.menu;
    }

    public void addCardValueSelectButtons() {
        for (int i = 1; i <= MAX_CARD_VALUE; i++) {
            var data = buildButtonData(1, "" + i);
            menu.addItem(2, data,  "" + i);
        }
    }

    public void addColorSelectButtons() {
        for (Color color : Color.valueList) {
            var data = buildButtonData(1, color.shortName);
            menu.addItem(3, data,  color.shortName);
        }
    }

    String buildButtonData(int index, String value) {
        var args = new ArrayList<String>();
        commandArgs().arguments().stream()
                .limit(index + 1)
                .forEach(args::add);
        args.add(value);
        return String.join(" ", args);
    }

    CommandArguments commandArgs() {
        return session.commandArgs;
    }

}
