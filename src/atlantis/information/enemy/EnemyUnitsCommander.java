package atlantis.information.enemy;

import atlantis.architecture.Commander;

public class EnemyUnitsCommander extends Commander {
    @Override
    protected boolean handle() {
        EnemyUnitsUpdater.updateFoggedUnits();
        return false;
    }
}
