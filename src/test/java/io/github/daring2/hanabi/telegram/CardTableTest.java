package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Card;
import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.telegram.CardTable.Row;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardTableTest {

    @Test
    void testTableText() {
        var table = new CardTable();

        table.addRow("p1", List.of(
                new Card(Color.WHITE, 1),
                new Card(Color.RED, 2)
        ));
        assertThat(table.buildText()).isEqualTo(
                "p1: W-1 R-2"
        );

        table.addRow("p002", List.of(
                new Card(Color.GREEN, 3)
        ));
        assertThat(table.buildText()).isEqualTo(
                "  p1: W-1 R-2\np002: G-3"
        );
    }

    @Test
    void testCalculateLabelPad() {
        var table = new CardTable();
        table.addRow("p1", List.of());
        assertThat(table.calculateLabelPad()).isEqualTo(2);
        table.addRow("p002", List.of());
        assertThat(table.calculateLabelPad()).isEqualTo(4);
        table.addRow("o03", List.of());
        assertThat(table.calculateLabelPad()).isEqualTo(4);
    }

    @Test
    void testBuildRowText() {
        var table = new CardTable();
        var row = new Row("p1", List.of(
                new Card(Color.WHITE, 1),
                new Card(Color.RED, 2),
                new Card(Color.GREEN, 3)
        ));
        assertThat(table.buildRowText(row, 5)).isEqualTo(
                "   p1: W-1 R-2 G-3"
        );
    }

}