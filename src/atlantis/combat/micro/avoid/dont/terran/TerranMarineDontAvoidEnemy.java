package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;

public class TerranMarineDontAvoidEnemy extends Manager {
    public TerranMarineDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMarine()) return false;

        if (protectMainChokeDuringMissionDefend()) return true;

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

        return false;
    }

    private boolean protectMainChokeDuringMissionDefend() {
        if (
            unit.isMissionDefend()
                && unit.hp() >= 22
                && unit.mission().focusPoint() != null
                && unit.mission().focusPoint().isAroundChoke()
        ) {
            return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
        (new AttackNearbyEnemies(unit)).forceHandle();

        return usedManager(this);
    }
}
