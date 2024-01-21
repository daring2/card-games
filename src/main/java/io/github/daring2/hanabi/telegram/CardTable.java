package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Card;
import io.github.daring2.hanabi.model.Player;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.rightPad;

class CardTable {

    final Player targetPlayer;
    final boolean maskCards;

    final List<Row> rows = new ArrayList<>();

    CardTable(Player targetPlayer, boolean maskCards) {
        this.targetPlayer = targetPlayer;
        this.maskCards = maskCards;
    }

    void addRow(String label, List<Card> cards) {
        rows.add(new Row(null, label, cards));
    }

    void addRow(Player player) {
        rows.add(new Row(
                player,
                player.name(),
                player.cards()
        ));
    }

    String buildText() {
        var labelPad = calculateLabelPad();
        return IntStream.range(0, rows.size())
                .mapToObj(i -> buildRowText(i, labelPad))
                .collect(Collectors.joining("\n"));
    }

    int calculateLabelPad() {
        return rows.stream()
                .mapToInt(it -> it.label.length())
                .max()
                .orElse(0);
    }

    String buildRowText(int rowIndex, int labelPad) {
        var cells = new ArrayList<String>();
        var row = rows.get(rowIndex);
        cells.add((rowIndex + 1) + ".");
        cells.add(rightPad(row.label, labelPad));
        row.cards.forEach(card -> {
            cells.add(buildCardText(row.player, card));
        });
        return cells.stream()
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(" "));
    }

    String buildCardText(Player player, Card card) {
        if (card.value() <= 0)
            return null;
        var isTargetPlayer = player != null && player == targetPlayer;
        if (maskCards && isTargetPlayer) {
            var cardInfo = player.getKnownCard(card);
            return cardInfo.toString();
        } else {
            return card.toString();
        }
    }

    record Row(
            Player player,
            String label,
            List<Card> cards
    ) {}

}
