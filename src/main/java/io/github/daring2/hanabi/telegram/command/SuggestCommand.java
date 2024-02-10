package io.github.daring2.hanabi.telegram.command;

import io.github.daring2.hanabi.model.Color;
import io.github.daring2.hanabi.telegram.ActionMenu;
import io.github.daring2.hanabi.telegram.UserSession;

import static io.github.daring2.hanabi.model.Game.MAX_CARD_VALUE;
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
                addCardValueSelectMenu(arguments);
                addColorSelectMenu(arguments);
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
            if (player == session.player())
                continue;
            var data = name + " " + (i + 1);
            var isSelected = i == selectedIndex;
            session.menu().addItem(1, new ActionMenu.Item(
                    data,  player.name(), isSelected, true
            ));
        }
    }

    void addCardValueSelectMenu(CommandArguments arguments) {
        var playerIndex = arguments.get(1);
        for (int i = 1; i <= MAX_CARD_VALUE; i++) {
            var data = name + " " + playerIndex + " " + i;
            session.menu().addItem(2, data,  "" + i);
        }
    }

    void addColorSelectMenu(CommandArguments arguments) {
        var playerIndex = arguments.get(1);
        for (Color color : Color.valueList) {
            var data = name + " " + playerIndex + " " + color.shortName;
            session.menu().addItem(3, data,  color.shortName);
        }
    }

    public boolean isVisibleInMenu() {
        if (!isCurrentPlayer())
            return false;
        return game().blueTokens() > 0;
    }

    @Override
    public boolean isVisibleInKeyboard() {
        return isVisibleInMenu();
    }

}
