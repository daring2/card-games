package io.github.daring2.hanabi.model;

import java.util.List;

public enum Color {

    WHITE, RED, GREEN, BLUE, YELLOW;

    public final String shortName = "" + name().charAt(0);

    public static final List<Color> valueList = List.of(values());

    public static Color findByShortName(String shortName) {
        return valueList.stream()
                .filter(it -> it.shortName.equals(shortName))
                .findAny().orElse(null);
    }

}
