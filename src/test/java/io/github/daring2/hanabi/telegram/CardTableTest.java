package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Card;
import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.telegram.CardTable.Row;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.daring2.hanabi.model.GameTestUtils.newPlayer;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

class CardTableTest {

    @Test
    void testAddRow() {
        var table = new CardTable(null);
        var cards = rangeClosed(1, 5)
                .mapToObj(i -> new Card(Color.WHITE, i))
                .toList();
        table.addRow("p1", cards.subList(0, 2));
        assertThat(table.rows).containsExactly(
                new Row(null, "p1", cards.subList(0, 2))
        );

        var player2 =  newPlayer("p2", cards.subList(2, 4));
        table.addRow(player2);
        assertThat(table.rows).containsExactly(
                new Row(null, "p1", cards.subList(0, 2)),
                new Row(player2, "p2", cards.subList(2, 4))
        );
    }

    @Test
    void testTableText() {
        var table = new CardTable(null);

        table.addRow("p1", List.of(
                new Card(Color.WHITE, 1),
                new Card(Color.RED, 2)
        ));
        assertThat(table.buildText()).isEqualTo(
                "p1 W-1 R-2"
        );

        table.addRow("p002", List.of(
                new Card(Color.GREEN, 3)
        ));
        assertThat(table.buildText()).isEqualTo(
                "p1   W-1 R-2\np002 G-3"
        );
    }

    @Test
    void testCalculateLabelPad() {
        var table = new CardTable(null);
        table.addRow("p1", List.of());
        assertThat(table.calculateLabelPad()).isEqualTo(2);
        table.addRow("p002", List.of());
        assertThat(table.calculateLabelPad()).isEqualTo(4);
        table.addRow("o03", List.of());
        assertThat(table.calculateLabelPad()).isEqualTo(4);
    }

    @Test
    void testBuildRowText() {
        var table = new CardTable(null);
        var row = new Row(null, "p1", List.of(
                new Card(Color.WHITE, 1),
                new Card(Color.RED, 2),
                new Card(Color.GREEN, 3)
        ));
        assertThat(table.buildRowText(row, 5)).isEqualTo(
                "p1    W-1 R-2 G-3"
        );
    }

    @Test
    void testBuildCardText() {
        var table = new CardTable(null);
        checkCardText(table, new Card(Color.WHITE, 0), null);
        checkCardText(table, new Card(Color.WHITE, 1), "W-1");
        checkCardText(table, new Card(Color.RED, 2), "R-2");
    }

    void checkCardText(CardTable table, Card card, String text) {
        assertThat(table.buildCardText(card)).isEqualTo(text);
    }

}