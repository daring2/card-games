package io.github.daring2.hanabi.model;

import jakarta.annotation.Nullable;

public record CardInfo(
        @Nullable
        Color color,
        int value
) {
        public static final int NULL_VALUE = 0;
        public static final CardInfo EMPTY = new CardInfo(null, NULL_VALUE);

        public CardInfo(Color color) {
                this(color, NULL_VALUE);
        }

        public CardInfo(int value) {
                this(null, value);
        }

        public boolean isValidForSuggest() {
                return color == null || value == NULL_VALUE;
        }

        CardInfo merge(CardInfo info) {
                return new CardInfo(
                        color != null ? color : info.color,
                        value != NULL_VALUE ? value : info.value
                );
        }

        @Override
        public String toString() {
                var result = "";
                result += color != null ? color.name().charAt(0) : "?";
                result += "-";
                result += value != NULL_VALUE ? value : "?";
                return result;
        }

}
