package atlantis.combat.squad.squad_scout;

import atlantis.architecture.Manager;
import atlantis.terran.chokeblockers.NeedChokeBlockers;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class SquadScoutSafety extends Manager {
    private Selection enemies;

    public SquadScoutSafety(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        enemies = unit.enemiesNear().canAttack(unit, 3.2 - (unit.hasCooldown() ? 0 : 0.7));
        return enemies.notEmpty();
    }

    @Override
    protected Manager handle() {
        if (NeedChokeBlockers.check()) {
            unit.runningManager().runFrom(
                enemies.nearestTo(unit), 3, Actions.RUN_ENEMY, false
            );
        }
        else {
            unit.move(unit.squadCenter(), Actions.RUN_ENEMY, "ScoutSafety");
        }

        return usedManager(this);
    }
}
