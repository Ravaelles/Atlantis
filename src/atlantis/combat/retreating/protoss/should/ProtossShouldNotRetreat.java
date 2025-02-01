package atlantis.combat.retreating.protoss.should;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.dont.protoss.DontAvoidWhenCannonsNear;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

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

        if (dontRetreatAsRangedAgainstMelee()) {
            unit.addLog("RangedVsMelee");
            return true;
        }

//        if (true) return false;

        if (unit.isDragoon() && AsDragoonDoNotRetreat.doNotRetreat(unit)) {
            unit.addLog("GoGoGoon");
            return true;
        }

        double eval = unit.eval();

        if (eval >= 0.98 && unit.hp() >= 32) {
            unit.addLog("HighEval");
            return true;
        }

        if (unit.friendsNear().combatUnits().countInRadius(6, unit) >= 3 && eval >= 1.5) {
            unit.addLog("NoRunHighEval");
            return true;
        }

//        if (unit.cooldown() <= 4 && unit.hp() >= 35 && unit.isMelee() && (unit.distToBase() <= 8 || unit.distToMain() <= 20)) {
//            unit.addLog("NoRunNearBase");
//            return true;
//        }

        if (DontAvoidWhenCannonsNear.check(unit)) {
            unit.addLog("DontRetreatSupportCannon");
            return true;
        }

        if (dontRunNearBases()) {
            unit.addLog("NoRetreatNearBase");
            return true;
        }

        if (shouldNotRunInMissionSparta(unit)) {
            unit.addLog("NoRunInSparta");
            return true;
        }

//        System.out.println("PRINT CanRetreat");
//        unit.addLog("CanRetreat");
//        unit.log().print();

//        if (shouldNotRunInMissionDefend(unit)) {
//            unit.addLog("NoRunInDefend");
//            return true;
//        }

//        if (eval <= 1.25) return false;

//        if (shouldNotRunInMissionAttack(unit)) {
//            unit.addLog("NoRunInAttack");
//            return true;
//        }

        return false;
    }

    private boolean dontRetreatAsRangedAgainstMelee() {
        if (!unit.isRanged()) return false;
        if (unit.hp() <= 22) return false;

        Selection enemies = unit.enemiesNear();

        if (Enemy.zerg()) {
            if (enemies.zerglings().countInRadius(5, unit) >= 3) return false;
        }

        return enemies.melee().notEmpty() && enemies.ranged().canAttack(unit, 6).empty();
    }

    private boolean dontRunNearBases() {
        return unit.hp() >= 30
            && unit.cooldown() <= 2
            && unit.friendsNear().bases().inRadius(4.5, unit).notEmpty();
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
            && unit.eval() >= 0.7
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
