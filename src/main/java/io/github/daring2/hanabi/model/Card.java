package io.github.daring2.hanabi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
        return color.shortName + "-" + value;
    }

    @JsonValue
    String toJson() {
        return toString();
    }

    @JsonCreator
    static Card fromJson(String json) {
        var color = Color.findByShortName(json.substring(0, 1));
        var value = Integer.parseInt(json.substring(2, 3));
        return new Card(color, value);
    }

}
