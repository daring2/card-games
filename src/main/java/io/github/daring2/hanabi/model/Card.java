package io.github.daring2.hanabi.model;

import static java.util.Objects.requireNonNull;

public record Card(
        Color color,
        int value
) {

    public Card {
        requireNonNull(color, "color");
    }

    @Override
    public String toString() {
        return color.name().charAt(0) + "-" + value;
    }

}
