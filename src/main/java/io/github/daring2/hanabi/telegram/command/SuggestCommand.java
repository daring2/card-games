package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.telegram.ActionMenu;
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
            addPlayerSelectMenu(arguments);
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

    void addPlayerSelectMenu(CommandArguments arguments) {
        var players = session.game().players();
        var selectedIndex = arguments.getIndexValue(1);
        for (int i = 0, size = players.size(); i < size; i++) {
            var player = players.get(i);
//            if (player == session.player())
//                continue;
            var data = name + " " + (i + 1);
            var isSelected = i == selectedIndex;
            session.menu().addItem(1, new ActionMenu.Item(
                    data,  player.name(), isSelected
            ));
        }
    }

    public boolean isVisibleInMenu() {
        if (!isCurrentPlayer())
            return false;
        return game().blueTokens() > 0;
    }

}
