package io.github.daring2.hanabi.model;

import io.github.daring2.hanabi.model.event.SuggestEvent;

import static io.github.daring2.hanabi.model.GameUtils.validate;

class SuggestAction extends PlayerAction {

    final Player targetPlayer;
    final CardInfo info;

    public SuggestAction(Game game, Player player, Player targetPlayer, CardInfo info) {
        super(game, player);
        this.targetPlayer = targetPlayer;
        this.info = info;
    }

    @Override
    void perform() {
        validate(targetPlayer != player, "invalid_target_player");
        validate(info.isValidForSuggest(), "invalid_suggestion");
        validate(game.blueTokens > 0, "no_blue_tokens_available");
        publishEvent(new SuggestEvent(game, player, targetPlayer, info));
        game.blueTokens--;
        targetPlayer.addCardInfo(info);
    }

}
