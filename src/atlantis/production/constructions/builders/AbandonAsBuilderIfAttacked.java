package atlantis.production.constructions.builders;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.AConsole;
import atlantis.util.log.LogLevel;

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
            unit.orderSink().stop(unit);
            AConsole.println(LogLevel.LOW, "STOP CONSTRUCTING");
            return usedManager(this, "StopConstructing");
        }

        AUnit enemy = unit.enemiesNear().nearestTo(unit);
        if (enemy != null) {
            unit.runningManager().runFrom(enemy, 5, Actions.RUN_ENEMY, false);
            AConsole.println(LogLevel.LOW, "AbandonAsBuilderIfAttacked");
            return usedManager(this, "AbandonAsBuilderIfAttacked");
        }

        return null;
    }
}
