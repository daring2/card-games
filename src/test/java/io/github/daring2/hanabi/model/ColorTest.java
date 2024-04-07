package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColorTest {

    @Test
    void testShortName() {
        assertThat(Color.WHITE.shortName).isEqualTo("W");
        assertThat(Color.RED.shortName).isEqualTo("R");
    }

    @Test
    void testFindByShortName() {
        assertThat(Color.findByShortName("W")).isEqualTo(Color.WHITE);
        assertThat(Color.findByShortName("R")).isEqualTo(Color.RED);
        assertThat(Color.findByShortName("A")).isNull();
    }

}