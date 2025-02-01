package atlantis.units.special.ums;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.squad.positioning.protoss.ProtossTooFarFromSquadCenter;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AliveEnemies;
import atlantis.util.PauseAndCenter;
import atlantis.util.We;

public class FixInvalidTargets extends Manager {
    public FixInvalidTargets(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isCombatUnit()) return false;
//        if (true) return false;

        if (
            unit.isAttacking()
                && unit.target() == null
                && unit.action().isAttacking()
                && unit.lastPositionChangedAgo() >= 5
                && unit.noCooldown()
        ) {
            if (A.isUms() && AliveEnemies.get().notEmpty()) {
                A.errPrintln("FixStoppedUnits: " + unit + " is attacking null target");
            }
//            PauseAndCenter.on(unit, true);
            return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
//        if ((new AttackNearbyEnemies(unit)).invokeFrom(this) != null) return usedManager(this);
        if ((new AttackNearbyEnemies(unit)).forceHandle() != null) return usedManager(this);

        if (We.protoss()) {
            if ((new ProtossTooFarFromSquadCenter(unit)).invokedFrom(this)) return usedManager(this);
        }

        return null;
    }
}
