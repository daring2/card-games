package io.github.daring2.hanabi.model;

class GameUtils {

    static void validate(boolean expression, String code, Object... args) {
        if (!expression) {
            throw new GameException(code, args);
        }
    }

    private GameUtils() {
    }

}
