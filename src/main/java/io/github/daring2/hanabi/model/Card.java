package io.github.daring2.hanabi.model;

public record Card(
        Color color,
        int value
) {

    @Override
    public String toString() {
        return color.name().charAt(0) + "-" + value;
    }

}
