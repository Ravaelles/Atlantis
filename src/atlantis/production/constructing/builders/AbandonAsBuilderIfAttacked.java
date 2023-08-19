package atlantis.production.constructing.builders;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class AbandonAsBuilderIfAttacked extends Manager {
    public AbandonAsBuilderIfAttacked(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWorker() && unit.isConstructing() && unit.hp() < 40;
    }

    @Override
    public Manager handle() {
        if (unit.isConstructing()) {
            unit.u().stop();
            System.err.println("STOP CONSTRUCTING");
            return usedManager(this, "StopConstructing");
        }

        AUnit enemy = unit.enemiesNear().nearestTo(unit);
        if (enemy != null) {
            unit.runningManager().runFrom(enemy, 5, Actions.RUN_ENEMY, false);
            System.err.println("AbandonAsBuilderIfAttacked");
            return usedManager(this, "AbandonAsBuilderIfAttacked");
        }

        return null;
    }
}
