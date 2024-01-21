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
    final InlineKeyboardMarkupBuilder markup;

    ActionKeyboard(UserSession session, UserCommand command) {
        this.session = session;
        this.command = command;
        this.markup = InlineKeyboardMarkup.builder();
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
        markup.keyboardRow(buttons);
    }

    void addCardSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        var player = session.player;
        var cards = player.cards();
        for (int i = 0, size = cards.size(); i < size; i++) {
            var card = cards.get(i);
            var data = command.expression + " " + (i + 1);
            var text = "" + player.getKnownCard(card);
            buttons.add(createButton(data, text));
        }
        markup.keyboardRow(buttons);
    }

    void addPlayerSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        var players = session.game.players();
        for (int i = 0, size = players.size(); i < size; i++) {
            var player = players.get(i);
            if (player == session.player)
                continue;
            var data = command.expression + " " + (i + 1);
            buttons.add(createButton(data, player.name()));
        }
        markup.keyboardRow(buttons);
    }

    void addCardValueSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        for (int i = 0; i <= MAX_CARD_VALUE; i++) {
            var data = command.expression + " " + i;
            buttons.add(createButton(data, "" + i));
        }
        markup.keyboardRow(buttons);
    }

    void addColorSelectButtons() {
        var buttons = new ArrayList<InlineKeyboardButton>();
        for (Color color : Color.valueList) {
            var data = command.expression + " " + color.shortName;
            buttons.add(createButton(data, color.shortName));
        }
        markup.keyboardRow(buttons);
    }

    InlineKeyboardButton createButton(String data, String text) {
        return InlineKeyboardButton.builder()
                .callbackData(data)
                .text(text)
                .build();
    }

    void open() {
        var text = messages().getMessage("select_action");
        var sendMessage = SendMessage.builder()
                .chatId(session.chatId)
                .replyMarkup(markup.build())
                .text(text)
                .build();
        session.bot.executeSync(sendMessage);
    }

    void update(Message message) {
        var editMessage = EditMessageReplyMarkup.builder()
                .chatId(session.chatId)
                .messageId(message.getMessageId())
                .replyMarkup(markup.build())
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
