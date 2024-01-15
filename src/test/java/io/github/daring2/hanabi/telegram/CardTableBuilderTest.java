package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Card;
import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.telegram.CardTableBuilder.Row;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardTableBuilderTest {

    @Test
    void testBuildTableText() {
        var builder = new CardTableBuilder();

        builder.addRow("p1", List.of(
                new Card(Color.WHITE, 1),
                new Card(Color.RED, 2)
        ));
        assertThat(builder.buildTableText()).isEqualTo(
                "p1: W-1 R-2"
        );

        builder.addRow("p002", List.of(
                new Card(Color.GREEN, 3)
        ));
        assertThat(builder.buildTableText()).isEqualTo(
                "  p1: W-1 R-2\np002: G-3"
        );
    }

    @Test
    void testCalculateLabelPad() {
        var builder = new CardTableBuilder();
        builder.addRow("p1", List.of());
        assertThat(builder.calculateLabelPad()).isEqualTo(2);
        builder.addRow("p002", List.of());
        assertThat(builder.calculateLabelPad()).isEqualTo(4);
        builder.addRow("o03", List.of());
        assertThat(builder.calculateLabelPad()).isEqualTo(4);
    }

    @Test
    void testBuildRowLine() {
        var builder = new CardTableBuilder();
        var row = new Row("p1", List.of(
                new Card(Color.WHITE, 1),
                new Card(Color.RED, 2),
                new Card(Color.GREEN, 3)
        ));
        assertThat(builder.buildRowLine(row, 5)).isEqualTo(
                "   p1: W-1 R-2 G-3"
        );
    }

}