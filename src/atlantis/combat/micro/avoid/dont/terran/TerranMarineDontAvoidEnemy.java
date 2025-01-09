package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.units.AUnit;

public class
TerranMarineDontAvoidEnemy extends Manager {
    public TerranMarineDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isMarine() && !unit.isFirebat()) return false;

        if (dontAvoidRangedHavingAdvantage()) {
            unit.addLog("BraveVsRanged");
            return true;
        }

        if (noMeleeNearAndBattleAdvantage()) return dontAvoid();

        if (longDidntShootHydra()) return dontAvoid();
        if (protectMainChokeDuringMissionDefend()) return dontAvoid();
        if (unit.isMissionAttack() && protectTanksNearby()) return dontAvoid();

        if (unit.isDefenseMatrixed()) return dontAvoid();

        if (
            unit.isMissionDefend()
                && unit.isHealthy()
        ) {
            if (
                unit.friendsNear().inRadius(2, unit).count() >= 4
                    && unit.meleeEnemiesNearCount(3) == 0
            ) return dontAvoid();

            if (unit.nearestOurTankDist() <= 3 && unit.nearestEnemyDist() > 3) return dontAvoid();
        }

        if (unit.meleeEnemiesNearCount(1.8) > 0) return false;

        if (unit.isHealthy() && unit.friendsNear().groundUnits().inRadius(1, unit).atLeast(3)) return dontAvoid();

        return false;
    }

    private boolean dontAvoidRangedHavingAdvantage() {
        if (unit.meleeEnemiesNearCount(3.2) > 0) return false;

        double eval = unit.eval();
        if (eval >= 1.24) return true;
        if (unit.friendsNear().combatUnits().countInRadius(6, unit) >= 10) return true;

        return unit.hp() >= (unit.enemiesNear().dragoons().countInRadius(7, unit) <= 1 ? 11 : 25)
            && eval >= 1.15;
    }


    private boolean dontAvoid() {
//        if (unit.hp() <= 19) A.printStackTrace("Why");
        return true;
    }

    private boolean noMeleeNearAndBattleAdvantage() {
        return unit.cooldown() <= 3
            && unit.hp() >= 21
            && unit.meleeEnemiesNearCount(3.4) == 0
            && unit.eval() >= 1.15;
    }

    private boolean longDidntShootHydra() {
        return unit.lastAttackFrameMoreThanAgo(30 * 7) && unit.enemiesNear().hydras().notEmpty();
    }

    private boolean protectTanksNearby() {
        return unit.friendsNear().tanks().inRadius(6, unit).atLeast(2);
    }

    private boolean protectMainChokeDuringMissionDefend() {
        if (
            unit.isMissionDefend()
                && unit.hp() >= 18
                && unit.mission().focusPoint() != null
                && unit.mission().focusPoint().isAroundChoke()
        ) {
            return true;
        }

        if (unit.hp() >= 18 && unit.friendsNear().bunkers().inRadius(4, unit).count() > 0) return true;

        return false;
    }

    @Override
    public Manager handle() {
        (new AttackNearbyEnemies(unit)).forceHandle();

        return usedManager(this);
    }
}
