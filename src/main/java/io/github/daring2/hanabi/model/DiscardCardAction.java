package io.github.daring2.hanabi.model;

import io.github.daring2.hanabi.model.event.DiscardCardEvent;

import static io.github.daring2.hanabi.model.GameUtils.validate;

class DiscardCardAction extends PlayerAction {

    final int cardIndex;

    public DiscardCardAction(Game game, Player player, int cardIndex) {
        super(game, player);
        this.cardIndex = cardIndex;
    }

    @Override
    void perform() {
        checkCardIndex(cardIndex);
        validate(
                game.blueTokens < settings.maxBlueTokens,
                "all_blue_tokens_in_game"
        );
        var card = player.removeCard(cardIndex);
        game.discard.add(card);
        game.blueTokens++;
        publishEvent(new DiscardCardEvent(game, player, card));
        game.takeCard(player);
    }

}
