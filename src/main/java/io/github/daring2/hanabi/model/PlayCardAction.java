package io.github.daring2.hanabi.model;

import io.github.daring2.hanabi.model.event.PlayCardEvent;

class PlayCardAction extends PlayerAction {

    final int cardIndex;

    public PlayCardAction(Game game, Player player, int cardIndex) {
        super(game, player);
        this.cardIndex = cardIndex;
    }

    @Override
    void perform() {
        checkCardIndex(cardIndex);
        var card = player.removeCard(cardIndex);
        var tableCards = game.table.get(card.color());
        var lastValue = tableCards.getLast().value();
        var isValid = card.value() == lastValue + 1;
        publishEvent(new PlayCardEvent(game, player, card, isValid));
        if (isValid) {
            game.addCardToTable(card);
        } else {
            game.discard.add(card);
            game.addRedToken();
        }
        game.takeCard(player);
    }

}
