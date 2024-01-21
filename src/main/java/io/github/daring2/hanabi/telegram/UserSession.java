package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameException;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.model.Player;
import io.github.daring2.hanabi.model.event.CreateGameEvent;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

class UserSession {

    final HanabiBot bot;
    final User user;
    final Long chatId;

    String userName;
    Game game;
    Player player;
    GameEventProcessor eventProcessor;

    Message turnInfoMessage;

    UserSession(HanabiBot bot, User user, Long chatId) {
        this.bot = bot;
        this.user = user;
        this.chatId = chatId;
        this.userName = user.getUserName();
    }

    void processUpdate(Update update) {
        runWithLock(() -> {
            new UserCommandProcessor(this, update).process();
        });
    }

    void runWithLock(Runnable action) {
        var lock = game != null ? game : this; //TODO refactor
        synchronized (lock) {
            action.run();
        }
    }

    void updateUserName(String name) {
        userName = name;
        sendMessage("player_name_updated", userName);
    }

    void createGame() {
        leaveCurrentGame();
        game = bot.context.gameFactory().create();
        bot.games.put(game.id(), game);
        registerGameListener();
        game.eventBus().publish(new CreateGameEvent(game));
        createPlayer();
    }

    void joinGame(String gameId) {
        leaveCurrentGame();
        game = bot.games.get(gameId);
        if (game == null) {
            sendMessage("game_not_found", gameId);
            return;
        }
        registerGameListener();
        createPlayer();
    }

    void registerGameListener() {
        eventProcessor = new GameEventProcessor(this);
    }

    void createPlayer() {
        player = new Player(userName);
        game.addPlayer(player);
    }

    void showActionKeyboard() {
        if (turnInfoMessage == null)
            return;
        createActionKeyboard(UserCommand.empty())
                .update(turnInfoMessage);
    }

    void finishTurn() {
        deleteMessage(turnInfoMessage);
        turnInfoMessage = null;
    }

    ActionKeyboard createActionKeyboard(UserCommand command) {
        var keyboard = new ActionKeyboard(this, command);
        keyboard.addActionButtons();
        return keyboard;
    }

    void leaveCurrentGame() {
        if (game == null)
            return;
        game.removePlayer(player);
        eventProcessor.close();
        game = null;
    }

    Message sendMessage(String code, Object... args) {
        var text = messages().getMessage(code, args);
        return sendText(text);
    }

    Message sendText(String text, String parseMode) {
        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(parseMode)
                .build();
        return bot.executeSync(sendMessage);
    }

    Message sendText(String text) {
        return sendText(text, null);
    }

    void deleteMessage(Message message) {
        if (message == null)
            return;
        var deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(message.getMessageId())
                .build();
        bot.executeSync(deleteMessage);
    }

    Player getPlayer(int index) {
        var players = game.players();
        if (index < 0 || index >= players.size()) {
            throw new GameException("invalid_player_index");
        }
        return players.get(index);
    }

    GameMessages messages() {
        return bot.context.gameMessages();
    }

}
