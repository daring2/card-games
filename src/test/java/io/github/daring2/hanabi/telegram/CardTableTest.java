package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Card;
import io.github.daring2.hanabi.model.CardInfo;
import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.model.Player;
import io.github.daring2.hanabi.telegram.CardTable.Row;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.daring2.hanabi.model.GameTestUtils.addKnownCard;
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
        table.addRow("p1", List.of(
                new Card(Color.WHITE, 1),
                new Card(Color.RED, 2),
                new Card(Color.GREEN, 3)
        ));
        table.addRow("p2", List.of(
                new Card(Color.BLUE, 4)
        ));
        assertThat(table.buildRowText(0, 5)).isEqualTo(
                "1. p1    W-1 R-2 G-3"
        );
        assertThat(table.buildRowText(1, 3)).isEqualTo(
                "2. p2  B-4"
        );
    }

    @Test
    void testBuildCardText() {
        Player player0 = new Player("p0");
        Player player1 = new Player("p1");
        var table = new CardTable(player0);

        var card0 = new Card(Color.WHITE, 0);
        checkCardText(table, null, card0, null);
        checkCardText(table, player0, card0, null);
        checkCardText(table, player1, card0, null);

        var card1 = new Card(Color.WHITE, 1);
        checkCardText(table, null, card1, "W-1");
        checkCardText(table, player0, card1, "?-?");
        checkCardText(table, player1, card1, "W-1");

        addKnownCard(player0, card1, new CardInfo(Color.WHITE));
        checkCardText(table, null, card1, "W-1");
        checkCardText(table, player0, card1, "W-?");
        checkCardText(table, player1, card1, "W-1");

        addKnownCard(player0, card1, new CardInfo(Color.WHITE, 1));
        checkCardText(table, player0, card1, "W-1");
    }

    void checkCardText(
            CardTable table,
            Player player,
            Card card,
            String text
    ) {
        assertThat(table.buildCardText(player, card)).isEqualTo(text);
    }

}