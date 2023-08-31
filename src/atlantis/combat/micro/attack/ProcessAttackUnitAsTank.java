package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.combat.micro.terran.tank.sieging.ForceSiege;
import atlantis.production.dynamic.terran.tech.SiegeMode;
import atlantis.units.AUnit;

public class ProcessAttackUnitAsTank {
    public static boolean forTank(Manager manager, AUnit unit, AUnit target) {
        if (!unit.isTankUnsieged()) return false;
        if (!SiegeMode.isResearched()) return false;

        double distTo = unit.distTo(target);

        if (target.isCombatBuilding()) return vsCombatBuilding(manager, unit, target, distTo);

        if (isCrucialGroundUnit(distTo, unit, target) && isGoodDistToSiege(unit, distTo, target)) {
            ForceSiege.forceSiegeNow(manager, "SiegeReaver");
            return true;
        }

        if (distTo >= 12.01) {
            unit.setTooltip("UnsiegeToAttack");
            return TerranTank.wantsToUnsiege(unit);
        }

        return false;
    }

    private static boolean vsCombatBuilding(Manager manager, AUnit unit, AUnit target, double distTo) {
        if (target.isVisibleUnitOnMap() && distTo <= 11.7) {
            ForceSiege.forceSiegeNow(manager, "SiegeDem");
            return true;
        }
        else if (distTo <= 7.8) {
            ForceSiege.forceSiegeNow(manager, "SiegeClose");
            return true;
        }

        return false;
    }

    private static boolean isGoodDistToSiege(AUnit unit, double distTo, AUnit target) {
        double minDist = target.isMoving() && unit.isOtherUnitFacingThisUnit(target) ? 13.7 : 11.99;

        return distTo <= minDist && (distTo >= 8 || unit.hasCooldown());
    }

    private static boolean isCrucialGroundUnit(double distTo, AUnit unit, AUnit target) {
        return target.isReaver()
            || target.isTank()
            || (target.isDefiler() && unit.idIsEven())
            || (target.isLurker() && distTo >= 9.7);
    }
}