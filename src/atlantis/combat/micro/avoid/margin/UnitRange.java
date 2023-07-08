package atlantis.combat.micro.avoid.margin;

import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import bwapi.UpgradeType;

public class UnitRange {

    public  int unitRangeBonus() {
        if (unit.isEnemy()) {
            return forEnemy();
        }
        else if (unit.isOur()) {
            return forUs();
        }

        return 0;
    }

    private  int forUs() {
        if (unit.isDragoon()) {
            return ATech.isResearched(UpgradeType.Singularity_Charge) ? 2 : 0;
        }

        if (unit.isMarine()) {
            return ATech.isResearched(UpgradeType.U_238_Shells) ? 2 : 0;
        }

        if (unit.isGoliath()) {
            return ATech.isResearched(UpgradeType.Charon_Boosters) ? 2 : 0;
        }

        return 0;
    }

    private  int forEnemy(AUnit enemy) {
        if (enemy.isDragoon()) {
            return A.seconds() >= 290 ? 2 : 0;
        }

        if (enemy.isMarine()) {
            return A.seconds() >= 400 ? 1 : 0;
        }

        if (enemy.isGoliath()) {
            return A.seconds() >= 450 ? 2 : 0;
        }

        return 0;
    }
}
