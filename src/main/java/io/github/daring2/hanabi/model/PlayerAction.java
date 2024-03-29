package io.github.daring2.hanabi.model;

import io.github.daring2.hanabi.model.event.GameEvent;

import static io.github.daring2.hanabi.model.GameUtils.validate;

abstract class PlayerAction {

    final Game game;
    final GameSettings settings;
    final Player player;

    public PlayerAction(Game game, Player player) {
        this.game = game;
        this.settings = game.settings;
        this.player = player;
    }

    void execute() {
        game.checkActive();
        checkCurrentPlayer();
        perform();
        if (game.result != null)
            return;
        game.startNextTurn();
    }

    abstract void perform();

    void publishEvent(GameEvent event) {
        game.publishEvent(event);
    }

    void checkCurrentPlayer() {
        var currentPlayer = game.currentPlayer();
        validate(
                player == currentPlayer,
                "player_not_current", currentPlayer
        );
    }

    void checkCardIndex(int index) {
        if (index < 0 || index >= player.cards.size()) {
            throw new GameException("invalid_card_index");
        }
    }

}
