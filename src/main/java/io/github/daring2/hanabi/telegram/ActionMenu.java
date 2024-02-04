package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.GameMessages;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ActionMenu {

    final UserSession session;

    final List<List<Item>> items = new ArrayList<>();

    public ActionMenu(UserSession session) {
        this.session = session;
    }

    public void clear() {
        items.clear();
    }

    public void addItem(int row, Item item) {
        while(items.size() <= row)
            items.add(new ArrayList<>());
        items.get(row).add(item);
    }

    public void addItem(
            int row,
            String data,
            String label,
            boolean selected
    ) {
        addItem(row, new Item(data, label, selected));
    }

    public void addItem(int row, String data, String label) {
        addItem(row, data, label, false);
    }

    void open() {
        var text = messages().getMessage("select_action");
        var sendMessage = SendMessage.builder()
                .chatId(session.chatId)
                .replyMarkup(buildKeyboard())
                .text(text)
                .build();
        session.bot.executeSync(sendMessage);
    }

    void updateKeyboard(Message message) {
        var editMessage = EditMessageReplyMarkup.builder()
                .chatId(session.chatId)
                .messageId(message.getMessageId())
                .replyMarkup(buildKeyboard())
                .build();
        session.bot.executeSync(editMessage);
    }

    InlineKeyboardMarkup buildKeyboard() {
        var builder = InlineKeyboardMarkup.builder();
        for (var rowItems : items) {
            var buttons = rowItems.stream()
                    .map(this::createButton)
                    .toList();
            builder.keyboardRow(buttons);
        }
        return builder.build();
    }

    InlineKeyboardButton createButton(Item item) {
        var text = (item.selected ? "* " : "") + item.label;
        return InlineKeyboardButton.builder()
                .callbackData(item.data.trim())
                .text(text)
                .build();
    }

    void updateChatMenu() {
        //TODO check if menu is changed
        var commands = buildBotCommands();
        if (commands.isEmpty())
            return;
        var scope = BotCommandScopeChat.builder()
                .chatId(session.chatId)
                .build();
        var editMessage = SetMyCommands.builder()
                .commands(commands)
                .scope(scope)
                .build();
        session.bot.executeSync(editMessage);
    }

    List<BotCommand> buildBotCommands() {
        if (items.isEmpty())
            return List.of();
        return items.getFirst().stream()
                .map(this::createBotCommand)
                .toList();
    }

    BotCommand createBotCommand(Item item) {
        return BotCommand.builder()
                .command("/" + item.data)
                .description(item.label)
                .build();
    }

    GameMessages messages() {
        return session.messages();
    }

    public record Item(
            String data,
            String label,
            boolean selected
    ) {}


}
