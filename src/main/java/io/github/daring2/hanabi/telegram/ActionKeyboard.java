package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.telegram.command.CommandArguments;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup.InlineKeyboardMarkupBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static io.github.daring2.hanabi.model.Game.MAX_CARD_VALUE;

class ActionKeyboard {

    //TODO refactor, remove?

    final UserSession session;
    final InlineKeyboardMarkupBuilder markupBuilder;

    ActionKeyboard(UserSession session) {
        this.session = session;
        this.markupBuilder = InlineKeyboardMarkup.builder();
    }

    void reset() {
        markupBuilder.clearKeyboard();
    }

    void addActionButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        //TODO check game tokens
        var actionIds = getEnabledActionIds();
        for (String actionId : actionIds) {
            var label = messages().getMessage("actions." + actionId);
            var isSelected = actionId.equals(commandArgs().name());
            var text = (isSelected ? "* " : "") + label; // use "✅" char
            buttons.add(createButton(actionId, text));
        }
        markupBuilder.keyboardRow(buttons);
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

    void addCardSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        var player = session.player;
        var cards = player.cards();
        for (int i = 0, size = cards.size(); i < size; i++) {
            var card = cards.get(i);
            var data = commandArgs().name() + " " + (i + 1);
            var text = "" + player.getKnownCard(card);
            buttons.add(createButton(data, text));
        }
        markupBuilder.keyboardRow(buttons);
    }

    void addPlayerSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        var players = session.game.players();
        var selectedIndex = commandArgs().getIndexValue(1);
        for (int i = 0, size = players.size(); i < size; i++) {
            var player = players.get(i);
            if (player == session.player)
                continue;
            var data = commandArgs().name() + " " + (i + 1);
            var isSelected = i == selectedIndex;
            var text = (isSelected ? "* " : "") + player.name();
            buttons.add(createButton(data, text));
        }
        markupBuilder.keyboardRow(buttons);
    }

    void addCardValueSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        for (int i = 1; i <= MAX_CARD_VALUE; i++) {
            var data = buildButtonData(1, "" + i);
            buttons.add(createButton(data, "" + i));
        }
        markupBuilder.keyboardRow(buttons);
    }

    void addColorSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        for (Color color : Color.valueList) {
            var data = buildButtonData(1, color.shortName);
            buttons.add(createButton(data, color.shortName));
        }
        markupBuilder.keyboardRow(buttons);
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

    void open() {
        var text = messages().getMessage("select_action");
        var sendMessage = SendMessage.builder()
                .chatId(session.chatId)
                .replyMarkup(markupBuilder.build())
                .text(text)
                .build();
        session.bot.executeSync(sendMessage);
    }

    void update(Message message) {
        var markup = markupBuilder.build();
        var editMessage = EditMessageReplyMarkup.builder()
                .chatId(session.chatId)
                .messageId(message.getMessageId())
                .replyMarkup(markup)
                .build();
        session.bot.executeSync(editMessage);
    }

    CommandArguments commandArgs() {
        return session.commandArgs;
    }

    GameMessages messages() {
        return session.messages();
    }

}
