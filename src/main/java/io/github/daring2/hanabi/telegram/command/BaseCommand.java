package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.model.Game;
import io.github.daring2.hanabi.model.GameException;
import io.github.daring2.hanabi.model.Player;
import io.github.daring2.hanabi.telegram.ActionKeyboard;
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

    ActionKeyboard keyboard() {
        return session.keyboard();
    }

    void checkGameNotNull() {
        if (game() == null) {
            throw new GameException("game_is_null");
        }
    }

}
