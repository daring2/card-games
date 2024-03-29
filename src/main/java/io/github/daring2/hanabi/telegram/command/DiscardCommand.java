package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

public class DiscardCommand extends BaseCommand {

    public DiscardCommand(UserSession session) {
        super(session, "discard");
    }

    @Override
    public void execute(CommandArguments arguments) {
        checkGameNotNull();
        if (arguments.size() < 2) {
            session.resetMenu();
            addCardSelectMenu();
            session.updateKeyboard();
            return;
        }
        var cardIndex = arguments.getIndexValue(1);
        game().discardCard(player(), cardIndex);
    }

    public boolean isVisibleInMenu() {
        if (!isCurrentPlayer())
            return false;
        var blueTokens = game().blueTokens();
        var maxBlueTokens = game().settings().getMaxBlueTokens();
        return blueTokens < maxBlueTokens;
    }

    @Override
    public boolean isVisibleInKeyboard() {
        return isVisibleInMenu();
    }

}
