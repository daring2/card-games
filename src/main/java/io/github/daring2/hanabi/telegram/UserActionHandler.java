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

import java.util.List;

class UserActionHandler {

    final UserSession session;

    UserActionHandler(UserSession session) {
        this.session = session;
    }

    void showCurrentPlayerActions() {
        var text = messages().getMessage("select_action");
        var markup = InlineKeyboardMarkup.builder()
                .keyboardRow(createPlayerActionButtons())
                .build();
        sendInlineKeyboard(text, markup);
    }

    void processCallbackQuery(CallbackQuery query) {
        var actionData = query.getData();
        switch (actionData) {
            case "play_card", "discard" -> {
                var cardButtons = createCardButtons(session.player, actionData);
                var markup = InlineKeyboardMarkup.builder()
                        .keyboardRow(createPlayerActionButtons())
                        .keyboardRow(cardButtons)
                        .build();
                updateInlineKeyboard(query.getMessage(), markup);
            }
            default -> {}
        }
    }

    List<InlineKeyboardButton> createPlayerActionButtons() {
        //TODO mark selected action
        return List.of(
                createActionButton("play_card"),
                createActionButton("discard"),
                createActionButton("suggest")
        );
    }

    List<InlineKeyboardButton> createCardButtons(Player player, String actionData) {
        return player.cards().stream()
                .map(card -> createCardButton(player, card, actionData))
                .toList();
    }

    InlineKeyboardButton createActionButton(String actionId) {
        var text = messages().getMessage("actions." + actionId);
        return createInlineButton(actionId, text);
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
