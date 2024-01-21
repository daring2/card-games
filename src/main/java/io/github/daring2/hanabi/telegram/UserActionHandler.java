package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.model.Player;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.IntStream.rangeClosed;
import static org.apache.commons.lang3.StringUtils.startsWith;

class UserActionHandler {

    //TODO (!) refactor

    static final String PLAY_CARD = "play_card";
    static final String DISCARD = "discard";
    static final String SUGGEST = "suggest";

    final UserSession session;

    //TODO replace with currentCommand
    String currentAction;

    UserActionHandler(UserSession session) {
        this.session = session;
    }

    void showCurrentPlayerActions() {
        var markup = InlineKeyboardMarkup.builder()
                .keyboardRow(createPlayerActionButtons(""))
                .build();
        currentAction = null;
        showActions(markup);
    }

    void processCallbackQuery(CallbackQuery query) {
        var data = query.getData();
        if (data == null || data.equals(currentAction))
            return;
        if (startsWith(data, PLAY_CARD)) {
            processCardActionCallback(query);
        } else if (startsWith(data, DISCARD)) {
            processCardActionCallback(query);
        } else if (startsWith(data, SUGGEST)) {
            processSuggestCallback(query);
        }
    }

    void processCardActionCallback(CallbackQuery query) {
        var data = query.getData();
        var command = UserCommand.parse(data);
        if (command.arguments.size() == 2) {
            session.tryProcessCommand(command);
            resetCurrentAction(query.getMessage());
            return;
        }
        var markup = InlineKeyboardMarkup.builder();
        markup.keyboardRow(createPlayerActionButtons(data));
        markup.keyboardRow(createCardButtons(session.player, data));
        updateActions(query.getMessage(), markup.build());
        currentAction = data;
    }

    void processSuggestCallback(CallbackQuery query) {
        var data = query.getData();
        var command = UserCommand.parse(data);
        var argumentCount = command.arguments.size();
        if (argumentCount == 3) {
            session.tryProcessCommand(command);
            resetCurrentAction(query.getMessage());
            return;
        }

        var markup = InlineKeyboardMarkup.builder();
        markup.keyboardRow(createPlayerActionButtons(data));
        markup.keyboardRow(createPlayerSelectButtons(data));
        if (argumentCount == 2) {
            markup.keyboardRow(createCardValueButtons(data));
            markup.keyboardRow(createColorButtons(data));
        }
        updateActions(query.getMessage(), markup.build());
        currentAction = data;
    }

    void resetCurrentAction(Message message) {
        currentAction = null;
        var deleteMessage = DeleteMessage.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .build();
        session.bot.executeSync(deleteMessage);
    }

    List<InlineKeyboardButton> createPlayerActionButtons(String selectedAction) {
        //TODO check game tokens
        var actionIds = List.of(PLAY_CARD, DISCARD, SUGGEST);
        var buttons = new ArrayList<InlineKeyboardButton>();
        for (String actionId : actionIds) {
            var label = messages().getMessage("actions." + actionId);
            var isSelected = actionId.equalsIgnoreCase(selectedAction);
            var text = (isSelected ? "* " : "") + label; // use "✅" char
            buttons.add(createButton(actionId, text));
        }
        return buttons;
    }

    List<InlineKeyboardButton> createCardButtons(Player player, String actionData) {
        var buttons = new ArrayList<InlineKeyboardButton>();
        var cards = player.cards();
        for (int i = 0, size = cards.size(); i < size; i++) {
            var card = cards.get(i);
            var data = actionData + " " + (i + 1);
            var text = player.getKnownCard(card).toString();
            buttons.add(createButton(data, text));
        }
        return buttons;
    }

    List<InlineKeyboardButton> createPlayerSelectButtons(String actionData) {
        var buttons = new ArrayList<InlineKeyboardButton>();
        var players = session.game.players();
        for (int i = 0, size = players.size(); i < size; i++) {
            var player = players.get(i);
            if (player == session.player)
                continue;
            var data = actionData + " " + (i + 1);
            buttons.add(createButton(data, player.name()));
        }
        return buttons;
    }

    List<InlineKeyboardButton> createCardValueButtons(String actionData) {
        return rangeClosed(1, Game.MAX_CARD_VALUE)
                .mapToObj(v -> createCardValueButton(v, actionData))
                .toList();
    }

    InlineKeyboardButton createCardValueButton(int value, String actionData) {
        var data = actionData + " " + value;
        return createButton(data, "" + value);
    }

    List<InlineKeyboardButton> createColorButtons(String actionData) {
        return Color.valueList.stream()
                .map(c -> createColorButton(c, actionData))
                .toList();
    }

    InlineKeyboardButton createColorButton(Color color, String actionData) {
        var data = actionData + " " + color.shortName;
        return createButton(data, color.name());
    }

    InlineKeyboardButton createButton(String data, String text) {
        return InlineKeyboardButton.builder()
                .callbackData(data)
                .text(text)
                .build();
    }

    void showActions(InlineKeyboardMarkup markup) {
        var text = messages().getMessage("select_action");
        var sendMessage = SendMessage.builder()
                .chatId(session.chatId)
                .replyMarkup(markup)
                .text(text)
                .build();
        session.bot.executeSync(sendMessage);
    }

    void updateActions(Message message, InlineKeyboardMarkup markup) {
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
