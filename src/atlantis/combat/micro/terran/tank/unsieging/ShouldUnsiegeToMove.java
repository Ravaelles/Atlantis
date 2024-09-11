package atlantis.combat.micro.terran.tank.unsieging;

import atlantis.units.AUnit;

public class ShouldUnsiegeToMove {
    public static boolean shouldUnsiege(AUnit unit) {
        if (!unit.isSieged()) return false;

        if (unit.lastSiegedAgo() <= 30 * (4 + unit.id() % 4)) return false;

        if (unit.enemiesNear().groundUnits().combatUnits().inRadius(12, unit).notEmpty()) return false;

        return true;
    }
}
