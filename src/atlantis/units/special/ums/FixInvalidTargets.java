package atlantis.units.special.ums;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.units.AUnit;

public class FixInvalidTargets extends Manager {
    public FixInvalidTargets(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isCombatUnit()) return false;

        if (unit.action().isAttacking() && unit.target() == null) {
//            A.errPrintln("FixStoppedUnits: " + unit + " is attacking but target is null");
            return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
        AttackNearbyEnemies.clearCache();

        if ((new AttackNearbyEnemies(unit)).invokeFrom(this) != null) return usedManager(this);

        return null;
    }
}
