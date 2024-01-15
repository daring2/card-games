package io.github.daring2.hanabi.model;

import jakarta.annotation.Nullable;

public record CardInfo(
        @Nullable
        Color color,
        int value
) {
        public static final int NULL_VALUE = 0;

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

}
