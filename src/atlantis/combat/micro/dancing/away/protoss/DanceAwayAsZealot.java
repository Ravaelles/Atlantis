package atlantis.combat.micro.dancing.away.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import bwapi.Color;

public class DanceAwayAsZealot extends Manager {
    public DanceAwayAsZealot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isZealot()) return false;
        if (unit.cooldown() <= 10) return false;
        if (unit.isMissionSparta()) return false;
        if (unit.shieldDamageAtMost(19)) return false;

        if (unit.moreMeleeEnemiesThanOurUnits()) return true;

        if (unit.hp() >= 35) return false;
        if (dontApplyWhenRangedEnemiesNear()) return false;

        boolean fairlyWounded = unit.hp() <= 38;

        // @ToDo Tweak these values
        return unit.cooldown() >= (fairlyWounded ? 4 : 16)
            && (fairlyWounded || unit.lastUnderAttackLessThanAgo(60));
    }

    private boolean dontApplyWhenRangedEnemiesNear() {
        Selection rangedEnemies = unit.enemiesNear().ranged();

        if (rangedEnemies.empty()) return false;

        return unit.hp() >= 32 || rangedEnemies.inRadius(3, unit).notEmpty();
    }

    private int minMeleeEnemiesNear() {
        if (unit.hp() < 80) return 1;

        return 2;
    }

    @Override
    protected Manager handle() {
        AUnit enemy = nearestMeleeEnemy();
        if (enemy == null) return null;

        unit.paintCircleFilled(26, Color.Yellow);

        return danceAwayFrom(enemy) ? usedManager(this) : null;
    }

    private boolean danceAwayFrom(AUnit enemy) {
        return unit.runningManager().runFrom(
            enemy.position(), 1, Actions.MOVE_DANCE_AWAY, false
        );
    }

    private AUnit nearestMeleeEnemy() {
        return unit.meleeEnemiesNear().nearestTo(unit);
    }
}
