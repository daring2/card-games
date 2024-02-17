package io.github.daring2.hanabi.model;

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

    void checkCurrentPlayer() {
        game.checkCurrentPlayer(player);
    }

}
