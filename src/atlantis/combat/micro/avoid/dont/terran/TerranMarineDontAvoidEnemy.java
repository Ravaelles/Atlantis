package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.units.AUnit;

public class TerranMarineDontAvoidEnemy extends Manager {
    public TerranMarineDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

        if (!unit.isMarine()) return false;

        if (longDidntShootHydra()) return true;
        if (protectMainChokeDuringMissionDefend()) return true;
        if (unit.isMissionAttack() && protectTanksNearby()) return true;

        if (
            unit.isMissionDefend()
                && unit.isHealthy()
        ) {
            if (
                unit.friendsNear().inRadius(2, unit).count() >= 4
                    && unit.meleeEnemiesNearCount(3) == 0
            ) return true;

            if (unit.nearestOurTankDist() <= 3 && unit.nearestEnemyDist() > 3) return true;
        }

        if (unit.meleeEnemiesNearCount(1.8) > 0) return false;

        if (unit.isHealthy() && unit.friendsNear().groundUnits().inRadius(1, unit).atLeast(3)) return true;

        return false;
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
