package io.github.daring2.hanabi.model;

import jakarta.annotation.Nullable;

public record CardInfo(
        @Nullable
        Color color,
        @Nullable
        Integer value
) {

        public CardInfo(Color color) {
                this(color, null);
        }

        public CardInfo(Integer value) {
                this(null, value);
        }

}
