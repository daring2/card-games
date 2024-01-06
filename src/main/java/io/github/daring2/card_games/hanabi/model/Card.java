package io.github.daring2.card_games.hanabi.model;

public record Card(
        int value,
        Color color
) {

    @Override
    public String toString() {
        return color.name().charAt(0) + "-" + value;
    }

}
