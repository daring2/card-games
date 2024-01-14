package io.github.daring2.hanabi.model;

public class GameException extends RuntimeException {

    final String code;
    final Object[] arguments;

    public GameException(String code, Object... arguments) {
        super(code);
        this.code = code;
        this.arguments = arguments;
    }

    public String getCode() {
        return code;
    }

    public Object[] getArguments() {
        return arguments;
    }

}
