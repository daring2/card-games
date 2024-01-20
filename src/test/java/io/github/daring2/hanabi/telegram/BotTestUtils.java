package io.github.daring2.hanabi.telegram;

import org.telegram.telegrambots.meta.api.objects.User;

public class BotTestUtils {

    static UserSession newSession() {
        return new UserSession(null, new User(), 0L);
    }

    private BotTestUtils() {
    }

}
