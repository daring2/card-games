package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Card;
import io.github.daring2.hanabi.model.Player;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.rightPad;

class CardTable {

    final Player targetPlayer;

    final List<Row> rows = new ArrayList<>();

    public CardTable(Player targetPlayer) {
        this.targetPlayer = targetPlayer;
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
        return rows.stream()
                .map(row -> buildRowText(row, labelPad))
                .collect(Collectors.joining("\n"));
    }

    int calculateLabelPad() {
        return rows.stream()
                .mapToInt(it -> it.label.length())
                .max()
                .orElse(0);
    }

    String buildRowText(Row row, int labelPad) {
        var cells = new ArrayList<String>();
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
        if (player == targetPlayer) {
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
