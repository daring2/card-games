package io.github.daring2.hanabi.model;

import org.springframework.stereotype.Component;

@Component
public class GameFactory {

    final Context context;

    public GameFactory(Context context) {
        this.context = context;
    }

    public Game create() {
        var game = new Game(context.gameSettings);
        game.setDeck(context.deckFactory.create());
        return game;
    }

    @Component
    public record Context(
            GameSettings gameSettings,
            DeckFactory deckFactory
    ) {}

}
