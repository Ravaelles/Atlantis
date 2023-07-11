package atlantis.information.enemy;

import atlantis.architecture.Commander;

public class EnemyUnitsCommander extends Commander {
    @Override
    public void handle() {
        EnemyUnitsUpdater.updateFoggedUnits();
    }
}
