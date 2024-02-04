package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.telegram.command.CommandArguments;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static io.github.daring2.hanabi.model.Game.MAX_CARD_VALUE;

public class ActionKeyboard {

    //TODO refactor, remove?

    final UserSession session;
    final ActionMenu menu;

    ActionKeyboard(UserSession session) {
        this.session = session;
        this.menu = session.menu;
    }

    void reset() {
        menu.reset();
    }

    void addActionButtons() {
        //TODO check game tokens
        var actionIds = getEnabledActionIds();
        for (String actionId : actionIds) {
            var label = messages().getMessage("actions." + actionId);
            var isSelected = actionId.equals(commandArgs().name());
            menu.addItem(0, actionId, label, isSelected);
        }
    }

    List<String> getEnabledActionIds() {
        var game = session.game;
        var actionIds = new ArrayList<String>();
        actionIds.add("play_card");
        var blueTokens = game.blueTokens();
        var maxBlueTokens = game.settings().getMaxBlueTokens();
        if (blueTokens < maxBlueTokens)
            actionIds.add("discard");
        if (blueTokens > 0)
            actionIds.add("suggest");
        return actionIds;
    }

    public void addCardSelectButtons() {
        var player = session.player;
        var cards = player.cards();
        for (int i = 0, size = cards.size(); i < size; i++) {
            var card = cards.get(i);
            var data = commandArgs().name() + " " + (i + 1);
            var text = "" + player.getKnownCard(card);
            menu.addItem(1, data, text);
        }
    }

    public void addPlayerSelectButtons() {
        var players = session.game.players();
        var selectedIndex = commandArgs().getIndexValue(1);
        for (int i = 0, size = players.size(); i < size; i++) {
            var player = players.get(i);
            if (player == session.player)
                continue;
            var data = commandArgs().name() + " " + (i + 1);
            var isSelected = i == selectedIndex;
            menu.addItem(1, data,  player.name(), isSelected);
        }
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

    InlineKeyboardButton createButton(String data, String text) {
        return InlineKeyboardButton.builder()
                .callbackData(data.trim())
                .text(text)
                .build();
    }

    void update(Message message) {
        session.menu.update(message);
    }

    CommandArguments commandArgs() {
        return session.commandArgs;
    }

    GameMessages messages() {
        return session.messages();
    }

}
