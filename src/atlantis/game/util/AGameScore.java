package atlantis.game.util;

import atlantis.Atlantis;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class AGameScore {
    public static int scoreCurrentGame(boolean winner) {
        return (winner ? 0 : -5000)
            + (int) Math.pow(AGame.killsLossesResourceBalance(), 0.7)
            + ((1 + Atlantis.KILLED) / (Atlantis.LOST + 1)) * 100
            + bonusScoreFromHitPointsLeftForOurUnits()
            - AGame.timeSeconds();
    }

    private static int bonusScoreFromHitPointsLeftForOurUnits() {
        int count = Select.ourCombatUnits().count();
        if (count == 0) return 0;

        int bonus = 0;

        for (AUnit unit : Select.ourCombatUnits().list()) {
            if (!unit.isABuilding()) {
                bonus += unit.hpPercent();
            }
        }

        return (2 * bonus) / count;
    }
}
