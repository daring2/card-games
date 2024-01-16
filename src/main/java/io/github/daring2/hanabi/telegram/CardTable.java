package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Card;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.rightPad;

class CardTable {

    final List<Row> rows = new ArrayList<>();

    void addRow(String label, List<Card> cards) {
        rows.add(new Row(label, cards));
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
            cells.add(buildCardText(card));
        });
        return cells.stream()
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(" "));
    }

    String buildCardText(Card card) {
        if (card.value() <= 0)
            return null;
        return card.toString();
    }

    record Row(
            String label,
            List<Card> cards
    ) {}

}
