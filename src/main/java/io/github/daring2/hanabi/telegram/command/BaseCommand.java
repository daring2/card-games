package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameException;
import io.github.daring2.hanabi.model.Player;
import io.github.daring2.hanabi.telegram.UserSession;

public abstract class BaseCommand implements UserCommand {

    final UserSession session;
    final String name;

    public BaseCommand(UserSession session, String name) {
        this.session = session;
        this.name = name;
    }

    public String name() {
        return name;
    }

    Game game() {
        return session.game();
    }

    Player player() {
        return session.player();
    }

    void addCardSelectMenu() {
        var player = session.player();
        var cards = player.cards();
        for (int i = 0, size = cards.size(); i < size; i++) {
            var card = cards.get(i);
            var data = name + " " + (i + 1);
            var text = "" + player.getKnownCard(card);
            session.menu().addItem(1, data, text);
        }
    }

    boolean isGameActive() {
        return game() != null && game().isActive();
    }

    boolean isCurrentPlayer() {
        if (!isGameActive())
            return false;
        return game().currentPlayer() == session.player();
    }

    void checkGameNotNull() {
        if (game() == null) {
            throw new GameException("game_is_null");
        }
    }

}
