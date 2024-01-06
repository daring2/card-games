package io.github.daring2.hanabi.model;

public record Card(
        int value,
        Color color
) {

    @Override
    public String toString() {
        return color.name().charAt(0) + "-" + value;
    }

}
