package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Protoss_Reaver;

public class ProtossShuttleEmpty extends Manager {
    private AUnit target;

    public ProtossShuttleEmpty(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isShuttle()) return false;
        if (!unit.loadedUnits().isEmpty()) return false;

        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossShuttleAvoidAA.class,
            ProtossShuttleEmptyGoToReaver.class,
            ProtossShuttleEmptyAvoidEnemies.class,
        };
    }
}
