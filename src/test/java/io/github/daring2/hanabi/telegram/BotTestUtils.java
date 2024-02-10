package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import org.telegram.telegrambots.meta.api.objects.User;

public class BotTestUtils {

    public static UserSession createTestSession() {
        return new UserSession(null, new User(), 0L);
    }

    public static void setGame(UserSession session, Game game) {
        session.game = game;
    }

    private BotTestUtils() {
    }

}
