package io.github.daring2.hanabi.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.model.Color.RED;
import static io.github.daring2.hanabi.model.Color.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardTest {

    ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    void testConstructor() {
        assertThat(new Card(WHITE, 1)).satisfies(card -> {
            assertThat(card.color()).isEqualTo(WHITE);
            assertThat(card.value()).isEqualTo(1);
        });
        assertThatThrownBy(() -> new Card(null, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("color");
    }

    @Test
    void testToString() {
        assertThat(new Card(WHITE, 1)).hasToString("W-1");
        assertThat(new Card(RED, 2)).hasToString("R-2");
    }

    @Test
    void testJsonSerialization() throws Exception {
        checkJsonSerialization(new Card(WHITE, 1), "\"W-1\"");
        checkJsonSerialization(new Card(RED, 2), "\"R-2\"");
    }

    void checkJsonSerialization(Card card, String json) throws Exception {
        assertThat(jsonMapper.writeValueAsString(card)).isEqualTo(json);
        assertThat(jsonMapper.readValue(json, Card.class)).isEqualTo(card);
    }

}