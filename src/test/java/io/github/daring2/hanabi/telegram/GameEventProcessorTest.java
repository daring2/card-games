package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.Game;
import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.telegram.BotTestUtils.createTestSession;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

class GameEventProcessorTest {

    @Test
    void testCalculateScoreLevel() {
        try (var processor = newProcessor()) {
            checkScoreLLevel(processor, 0, 5, 0);
            checkScoreLLevel(processor, 6, 10, 1);
            checkScoreLLevel(processor, 11, 15, 2);
            checkScoreLLevel(processor, 16, 20, 3);
            checkScoreLLevel(processor, 21, 24, 4);
            checkScoreLLevel(processor, 21, 24, 4);
            checkScoreLLevel(processor, 25, 25, 5);
        }
    }

    void checkScoreLLevel(
            GameEventProcessor processor,
            int minScore,
            int maxScore,
            int expectedLevel
    ) {
        rangeClosed(minScore, maxScore).forEach(score -> {
            var level = processor.calculateScoreLevel(score);
            assertThat(level).isEqualTo(expectedLevel);
        });
    }

    GameEventProcessor newProcessor() {
        var session = createTestSession();
        session.game = new Game();
        return new GameEventProcessor(session);
    }

}