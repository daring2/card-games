package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

public class PlayCardCommand extends BaseCommand {

    public PlayCardCommand(UserSession session) {
        super(session, "play_card");
    }

    @Override
    public void execute(CommandArguments arguments) {
        checkGameNotNull();
        if (arguments.size() < 2) {
            session.resetMenu();
            addCardSelectMenuItems();
            session.updateKeyboard();
            return;
        }
        var cardIndex = arguments.getIndexValue(1);
        game().playCard(player(), cardIndex);
    }

    @Override
    public boolean isVisibleInMenu() {
        return isCurrentPlayer();
    }

}
