package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.UserSession;

import static io.github.daring2.hanabi.telegram.command.UserCommandUtils.parseCardInfo;

public class SuggestCommand extends BaseCommand {

    public SuggestCommand(UserSession session) {
        super(session, "suggest");
    }

    @Override
    public void execute(CommandArguments arguments) {
        checkGameNotNull();
        var argumentsCount = arguments.size();
        if (argumentsCount < 3) {
            session.resetMenu();
            keyboard().addPlayerSelectButtons();
            if (argumentsCount == 2) {
                keyboard().addCardValueSelectButtons();
                keyboard().addColorSelectButtons();
            }
            session.updateKeyboard();
            return;
        }
        var playerIndex = arguments.getIndexValue(1);
        var targetPlayer = session.getPlayer(playerIndex);
        var cardInfo = parseCardInfo(arguments.get(2));
        game().suggest(player(), targetPlayer, cardInfo);
    }

    public boolean isVisibleInMenu() {
        if (!isCurrentPlayer())
            return false;
        return game().blueTokens() > 0;
    }

}
