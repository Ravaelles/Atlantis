package atlantis.combat.micro.terran.infantry.bunker;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class ConsiderLoadingIntoBunkers extends Manager {
    public ConsiderLoadingIntoBunkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isLoaded()) return false;
        if (Count.bunkers() == 0) return false;
        if (unit.hp() <= 20 && Count.marines() >= 5 && unit.meleeEnemiesNearCount(1.7) > 0) return false;
        if (!unit.isMarine() && !unit.isGhost()) return false;
        if (unit.isWounded() && unit.meleeEnemiesNearCount(1.9) >= 2) return false;

        boolean noBunkerVeryNear = unit.friendsNear().bunkers().inRadius(4.5, unit).empty();

        if (!noBunkerVeryNear && unit.distToFocusPoint() <= 4) return true;

        if (unit.hp() >= 33 && noBunkerVeryNear && unit.noCooldown()) {
            if (unit.enemiesNear().inRadius(1.6, unit).notEmpty()) return false;
            if (unit.friendsInRadius(0.2).nonBuildings().count() >= 3) return false;
        }

        if (unit.hpMoreThan(20)) {
            double distToFocusPoint = unit.distToFocusPoint();
            if (distToFocusPoint >= 8 && unit.enemiesNear().inRadius(14, unit).empty()) return false;
            if (unit.enemiesNear().inRadius(8, unit).empty() && !Enemy.terran()) return false;

            if (unit.isMissionDefend()) {
                if (
                    unit.hasCooldown()
                        || (unit.hp() <= 20 && unit.enemiesNear().canAttack(unit, 3).notEmpty())
                ) {
                    return true;
                }
            }
        }

        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            LoadIntoTheBunker.class,
        };
    }
}
