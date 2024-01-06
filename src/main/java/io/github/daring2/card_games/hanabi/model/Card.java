package io.github.daring2.card_games.hanabi.model;

public record Card(
        int value,
        Color color
) {

    @Override
    public String toString() {
        return color.name().substring(0, 1) + value;
    }

}
