package atlantis.combat.retreating.protoss.should;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.dont.protoss.DontAvoidWhenCannonsNear;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class ProtossDontRetreat extends Manager {
    public ProtossDontRetreat(AUnit unit) {
        super(unit.squadLeaderOrThisUnit());
    }

    @Override
    public boolean applies() {
        if (unit.isAir()) return true;
        if (unit.isDarkTemplar()) return true;

        return unit.enemiesNear().visibleOnMap().notEmpty()
            && dontRetreat();
    }

    @Override
    protected Manager handle() {
        if ((new AttackNearbyEnemies(unit)).invokeFrom(this) != null) return usedManager(this);

        return null;
    }

    private boolean t(String reasonWhyYes) {
//        System.err.println("ProtossDontRetreat because " + reasonWhyYes);
        return true;
    }

    public boolean dontRetreat() {
        if (unit.isMissionDefendOrSparta()) return t("Mission defend or Sparta");
        if (unit.isRunning()) return t("Already running");

        if (A.supplyUsed() >= 193 && A.hasMinerals(300)) {
            unit.addLog("HugeSupply");
            return t("HugeSupply");
        }

        if (dontRetreatAsRangedAgainstMelee()) {
            unit.addLog("RangedVsMelee");
            return t("RangedVsMelee");
        }

//        if (true) return false;

        if (unit.isDragoon() && AsDragoonDoNotRetreat.doNotRetreat(unit)) {
            unit.addLog("GoGoGoon");
            return t("GoGoGoon");
        }

        if (DontAvoidWhenCannonsNear.check(unit)) {
            unit.addLog("DontRetreatSupportCannon");
            return t("DontRetreatSupportCannon");
        }

        if (dontRunNearBases()) {
            unit.addLog("dontRunNearBases");
            return t("dontRunNearBases");
        }

        if (shouldNotRunInMissionSparta(unit)) {
            unit.addLog("NoRunInSparta");
            return t("NoRunInSparta");
        }

//        double threshold = 1.15 - (Count.bases() == 0 ? 0.2 : 0);
//
//        AUnit leader = unit.squadLeader();
//        if (leader != null) {
//            double eval = leader.eval();
//
//            if (eval >= threshold) {
//                return t("HighEval:" + eval);
//            }
//        }

        return false;
    }

    private boolean dontRetreatAsRangedAgainstMelee() {
        if (!unit.isRanged()) return false;
        if (unit.hp() <= Enemy.zergElse(22, 36) && unit.cooldown() <= 6) return false;

        Selection enemies = unit.enemiesNear();

//        if (Enemy.zerg()) {
//            if (enemies.zerglings().countInRadius(5, unit) >= 3) return false;
//        }
//        else if (Enemy.protoss()) {
        if (enemies.melee().countInRadius(5, unit) >= 3) return false;
//        }

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
