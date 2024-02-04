package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameException;
import io.github.daring2.hanabi.model.GameMessages;
import io.github.daring2.hanabi.model.Player;
import io.github.daring2.hanabi.model.event.CreateGameEvent;
import io.github.daring2.hanabi.telegram.command.CommandArguments;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.apache.commons.lang3.StringUtils.firstNonEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class UserSession {

    static final Logger logger = getLogger(UserSession.class);

    final HanabiBot bot;
    final User user;
    final Long chatId;

    final CommandProcessor commandProcessor = new CommandProcessor(this);
    final ActionKeyboard keyboard = new ActionKeyboard(this);

    String playerName;
    Game game;
    Player player;
    GameEventProcessor eventProcessor;

    CommandArguments commandArgs;
    Message turnInfoMessage;

    UserSession(HanabiBot bot, User user, Long chatId) {
        this.bot = bot;
        this.user = user;
        this.chatId = chatId;
        this.playerName = buildUserName(user);
    }

    String buildUserName(User user) {
        return firstNonEmpty(user.getUserName(), user.getFirstName());
    }

    public Game game() {
        return game;
    }

    public Player player() {
        return player;
    }

    public ActionKeyboard keyboard() {
        return keyboard;
    }

    void processUpdate(Update update) {
        runWithLock(() -> {
            commandProcessor.process(update);
        });
    }

    void runWithLock(Runnable action) {
        var lock = game != null ? game : this; //TODO refactor
        synchronized (lock) {
            action.run();
        }
    }

    public void updatePlayerName(String name) {
        playerName = name;
        sendMessage("player_name_updated", playerName);
    }

    public void createGame() {
        leaveCurrentGame();
        game = bot.context.gameFactory().create();
        bot.games.put(game.id(), game);
        registerGameListener();
        game.eventBus().publish(new CreateGameEvent(game));
        createPlayer();
    }

    public void joinGame(String gameId) {
        if (game != null && game.id().equals(gameId))
            return;
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
        player = new Player(playerName);
        game.addPlayer(player);
    }

    void showKeyboard() {
        if (turnInfoMessage == null)
            return;
        keyboard.reset();
        keyboard.addActionButtons();
        keyboard.update(turnInfoMessage);
    }

    public void updateKeyboard() {
        keyboard.update(turnInfoMessage);
    }

    void finishTurn() {
        try {
            deleteMessage(turnInfoMessage);
        } catch (Exception e) {
            logger.warn("Cannot delete message", e);
        }
        turnInfoMessage = null;
        commandProcessor.activeCommand = null;
    }

    public void leaveCurrentGame() {
        if (game == null)
            return;
        game.removePlayer(player);
        eventProcessor.close();
        game = null;
    }

    public Message sendMessage(String code, Object... args) {
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

    public Player getPlayer(int index) {
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
