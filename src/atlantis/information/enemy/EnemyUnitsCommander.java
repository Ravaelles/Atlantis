package atlantis.information.enemy;

import atlantis.architecture.Commander;

public class EnemyUnitsCommander extends Commander {
    @Override
    protected void handle() {
        EnemyUnitsUpdater.updateFoggedUnits();
    }
}
