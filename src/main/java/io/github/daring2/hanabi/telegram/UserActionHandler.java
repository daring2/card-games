package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Card;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.model.Player;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.startsWith;

class UserActionHandler {

    final UserSession session;

    String selectedAction;

    UserActionHandler(UserSession session) {
        this.session = session;
    }

    void showCurrentPlayerActions() {
        var text = messages().getMessage("select_action");
        var markup = InlineKeyboardMarkup.builder()
                .keyboardRow(createPlayerActionButtons(""))
                .build();
        selectedAction = null;
        sendInlineKeyboard(text, markup);
    }

    void processCallbackQuery(CallbackQuery query) {
        var data = query.getData();
        if (startsWith(data, selectedAction))
            return;
        var markup = InlineKeyboardMarkup.builder();
        markup.keyboardRow(createPlayerActionButtons(data));
        switch (data) {
            case "play_card", "discard" -> {
                markup.keyboardRow(createCardButtons(session.player, data));
            }
            case "suggest" -> {
                //TODO implement
            }
        }
        updateInlineKeyboard(query.getMessage(), markup.build());
        selectedAction = data;
    }

    List<InlineKeyboardButton> createPlayerActionButtons(String selectedAction) {
        var actionIds = List.of("play_card", "discard", "suggest");
        var buttons = new ArrayList<InlineKeyboardButton>();
        for (String actionId : actionIds) {
            var label = messages().getMessage("actions." + actionId);
            var isSelected = actionId.equalsIgnoreCase(selectedAction);
            var text = (isSelected ? "* " : "") + label; // use "✅" char
            buttons.add(createInlineButton(actionId, text));
        }
        return buttons;
    }

    List<InlineKeyboardButton> createCardButtons(Player player, String actionData) {
        return player.cards().stream()
                .map(card -> createCardButton(player, card, actionData))
                .toList();
    }

    InlineKeyboardButton createCardButton(
            Player player,
            Card card,
            String parentData
    ) {
        var data = parentData + " " + card;
        var text = player.getKnownCard(card).toString();
        return createInlineButton(data, text);
    }

    InlineKeyboardButton createInlineButton(String data, String text) {
        return InlineKeyboardButton.builder()
                .callbackData(data)
                .text(text)
                .build();
    }

    void sendInlineKeyboard(String text, InlineKeyboardMarkup markup) {
        var sendMessage = SendMessage.builder()
                .chatId(session.chatId)
                .replyMarkup(markup)
                .text(text)
                .build();
        session.bot.executeSync(sendMessage);
    }

    void updateInlineKeyboard(Message message, InlineKeyboardMarkup markup) {
        var editMessage = EditMessageReplyMarkup.builder()
                .chatId(session.chatId)
                .messageId(message.getMessageId())
                .replyMarkup(markup)
                .build();
        session.bot.executeSync(editMessage);
    }

    GameMessages messages() {
        return session.messages();
    }

}
