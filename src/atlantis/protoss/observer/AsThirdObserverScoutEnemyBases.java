package atlantis.protoss.observer;

import atlantis.architecture.Manager;
import atlantis.map.scout.ScoutPotentialEnemyBases;
import atlantis.map.scout.ScoutUnexploredBasesNearEnemy;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class AsThirdObserverScoutEnemyBases extends Manager {
    private Selection observers;

    public AsThirdObserverScoutEnemyBases(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isObserver()
            && Count.observers() >= 3
            && unit.getUnitIndexInBwapi() == 3;
    }

    @Override
    public Manager handle() {
        if ((new ScoutUnexploredBasesNearEnemy(unit)).invokedFrom(this)) {
            return usedManager(this);
        }

        if ((new ScoutPotentialEnemyBases(unit)).invokedFrom(this)) {
            return usedManager(this);
        }

        return null;
    }
}
