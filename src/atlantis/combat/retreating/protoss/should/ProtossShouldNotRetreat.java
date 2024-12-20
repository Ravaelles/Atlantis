package atlantis.combat.retreating.protoss.should;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.dont.protoss.DontAvoidWhenCannonsNear;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ProtossShouldNotRetreat extends Manager {
    public ProtossShouldNotRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().visibleOnMap().notEmpty()
            && shouldNotRetreat();
    }

    @Override
    protected Manager handle() {
        if ((new AttackNearbyEnemies(unit)).invokeFrom(this) != null) return usedManager(this);

        return null;
    }

    public boolean shouldNotRetreat() {
        if (A.supplyUsed() >= 193 && A.hasMinerals(300)) {
            unit.addLog("HugeSupply");
            return true;
        }

//        if (true) return false;

//        if (unit.isDragoon()) return AsDragoonDoNotRetreat.doNotRetreat(unit);

        if (unit.friendsNear().combatUnits().countInRadius(6, unit) >= 3 && unit.combatEvalRelative() >= 1.5) {
            unit.addLog("NoRunHighEval");
            return true;
        }

//        if (unit.cooldown() <= 4 && unit.hp() >= 35 && unit.isMelee() && (unit.distToBase() <= 8 || unit.distToMain() <= 20)) {
//            unit.addLog("NoRunNearBase");
//            return true;
//        }

        if (unit.cooldown() <= 4 && DontAvoidWhenCannonsNear.check(unit)) {
            unit.addLog("DontRetreatSupportCannon");
            return true;
        }

        if (shouldNotRunInMissionSparta(unit)) {
            unit.addLog("NoRunInSparta");
            return true;
        }

//        if (shouldNotRunInMissionDefend(unit)) {
//            unit.addLog("NoRunInDefend");
//            return true;
//        }

        if (unit.combatEvalRelative() <= 1.2) return false;

//        if (shouldNotRunInMissionAttack(unit)) {
//            unit.addLog("NoRunInAttack");
//            return true;
//        }

        return false;
    }

    private static boolean shouldNotRunInMissionSparta(AUnit unit) {
        return unit.isMissionSparta()
            && unit.isMelee()
            && unit.hp() >= 21
            && unit.distToNearestChokeLessThan(3);
    }

    private static boolean shouldNotRunInMissionDefend(AUnit unit) {
        if (!unit.isMissionDefend()) return false;

        return unit.isMelee()
            && unit.hp() >= 35
            && unit.cooldown() <= 10
            && unit.combatEvalRelative() >= 0.7
            && (
            unit.distToBase() <= 6
                ||
                closeToFriendlyCombatBuilding(unit)
        );
    }

    private static boolean shouldNotRunInMissionAttack(AUnit unit) {
        if (!unit.isMissionAttack()) return false;

        // Always consider retreating near enemy combat buildings
        if (unit.enemiesNear().combatBuildingsAnti(unit).notEmpty()) return false;

        if (unit.isZealot()) {
            AUnit nearestGoon = unit.friendsNear().dragoons().inRadius(4, unit).nearestTo(unit);
            if (nearestGoon == null) return false;

            return !nearestGoon.isRetreating();
        }

        if (AsDragoonDoNotRetreat.doNotRetreat(unit)) {
            return true;
        }

        return false;
    }

    private static boolean closeToFriendlyCombatBuilding(AUnit unit) {
        return unit.hp() > 20
            && unit.friendsNear().combatBuildings(false).inRadius(3, unit).notEmpty();
    }
}
