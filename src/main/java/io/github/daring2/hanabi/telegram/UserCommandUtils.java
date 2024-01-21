package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.CardInfo;
import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.model.GameException;

import static io.github.daring2.hanabi.model.Game.MAX_CARD_VALUE;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class UserCommandUtils {

    static CardInfo parseCardInfo(String infoExp) {
        if (isNumeric(infoExp)) {
            var value = parseInt(infoExp);
            if (value >= 1 && value <= MAX_CARD_VALUE)
                return new CardInfo(value);
        } else {
            for (Color color : Color.valueList) {
                if (color.shortName.equalsIgnoreCase(infoExp))
                    return new CardInfo(color);
                if (color.name().equalsIgnoreCase(infoExp))
                    return new CardInfo(color);
            }
        }
        throw new GameException("invalid_suggestion");
    }

    private UserCommandUtils() {
    }

}
