package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.GameMessages;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class UserActions {

    final UserSession session;

    public UserActions(UserSession session) {
        this.session = session;
    }

    void showCurrentPlayerActions() {
        var buttons = List.of(
                createActionButton("play_card"),
                createActionButton("discard"),
                createActionButton("suggest")
        );
        var text = messages().getMessage("select_action");
        var markup = InlineKeyboardMarkup.builder()
                .keyboardRow(buttons)
                .build();
        sendInlineKeyboard(text, markup);
    }

    InlineKeyboardButton createActionButton(String actionId) {
        var text = messages().getMessage("actions." + actionId);
        return createButton(actionId, text);
    }

    InlineKeyboardButton createButton(String data, String text) {
        return InlineKeyboardButton.builder()
                .callbackData(data)
                .text(text)
                .build();
    }

    void sendInlineKeyboard(String text, InlineKeyboardMarkup markup) {
        try {
            var sendMessage = SendMessage.builder()
                    .chatId(session.chatId)
                    .replyMarkup(markup)
                    .text(text)
                    .build();
            session.bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    GameMessages messages() {
        return session.messages();
    }

}
