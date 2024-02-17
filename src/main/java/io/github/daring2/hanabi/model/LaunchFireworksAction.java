package io.github.daring2.hanabi.model;

import static io.github.daring2.hanabi.model.GameUtils.validate;

class LaunchFireworksAction extends PlayerAction {

    public LaunchFireworksAction(Game game, Player player) {
        super(game, player);
    }

    @Override
    void perform() {
        validate(game.deck.isEmpty(), "deck_not_empty");
        game.finish(GameResult.LAUNCH);
    }

}
