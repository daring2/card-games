package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.model.GameMessages;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup.InlineKeyboardMarkupBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static io.github.daring2.hanabi.model.Game.MAX_CARD_VALUE;

class ActionKeyboard {

    final UserSession session;
    final UserCommand command;
    final InlineKeyboardMarkupBuilder markupBuilder;

    ActionKeyboard(UserSession session, UserCommand command) {
        this.session = session;
        this.command = command;
        this.markupBuilder = InlineKeyboardMarkup.builder();
    }

    void addActionButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        //TODO check game tokens
        var actionIds = List.of("play_card", "discard", "suggest");
        for (String actionId : actionIds) {
            var label = messages().getMessage("actions." + actionId);
            var isSelected = actionId.equals(command.name);
            var text = (isSelected ? "* " : "") + label; // use "✅" char
            buttons.add(createButton(actionId, text));
        }
        markupBuilder.keyboardRow(buttons);
    }

    void addCardSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        var player = session.player;
        var cards = player.cards();
        for (int i = 0, size = cards.size(); i < size; i++) {
            var card = cards.get(i);
            var data = command.name + " " + (i + 1);
            var text = "" + player.getKnownCard(card);
            buttons.add(createButton(data, text));
        }
        markupBuilder.keyboardRow(buttons);
    }

    void addPlayerSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        var players = session.game.players();
        var selectedIndex = command.getIndexArgument(1);
        for (int i = 0, size = players.size(); i < size; i++) {
            var player = players.get(i);
            if (player == session.player)
                continue;
            var data = command.name + " " + (i + 1);
            var isSelected = i == selectedIndex;
            var text = (isSelected ? "* " : "") + player.name();
            buttons.add(createButton(data, text));
        }
        markupBuilder.keyboardRow(buttons);
    }

    void addCardValueSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        for (int i = 1; i <= MAX_CARD_VALUE; i++) {
            var data = command.expression + " " + i;
            buttons.add(createButton(data, "" + i));
        }
        markupBuilder.keyboardRow(buttons);
    }

    void addColorSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        for (Color color : Color.valueList) {
            var data = command.expression + " " + color.shortName;
            buttons.add(createButton(data, color.shortName));
        }
        markupBuilder.keyboardRow(buttons);
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
        if (markup.equals(message.getReplyMarkup()))
            return; // TODO refactor
        var editMessage = EditMessageReplyMarkup.builder()
                .chatId(session.chatId)
                .messageId(message.getMessageId())
                .replyMarkup(markup)
                .build();
        session.bot.executeSync(editMessage);
    }

    void close(Message message) {
        var deleteMessage = DeleteMessage.builder()
                .chatId(session.chatId)
                .messageId(message.getMessageId())
                .build();
        session.bot.executeSync(deleteMessage);
    }

    GameMessages messages() {
        return session.messages();
    }

}
