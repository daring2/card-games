package io.github.daring2.hanabi.model;

public class TestPlayerAction extends PlayerAction {

    Runnable action;

    public TestPlayerAction(Game game, Player player) {
        super(game, player);
    }

    @Override
    void perform() {
        action.run();
    }

}
