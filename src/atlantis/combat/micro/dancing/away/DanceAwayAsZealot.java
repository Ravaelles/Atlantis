package atlantis.combat.micro.dancing.away;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class DanceAwayAsZealot extends Manager {
    public DanceAwayAsZealot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isZealot()) return false;

        return unit.cooldown() >= 10
            && unit.isWounded()
            && unit.lastUnderAttackLessThanAgo(20)
            && unit.meleeEnemiesNearCount(1.2) > minMeleeEnemiesNear();
    }

    private int minMeleeEnemiesNear() {
        if (unit.hp() < 80) return 1;

        return 2;
    }

    @Override
    protected Manager handle() {
        AUnit enemy = nearestMeleeEnemy();
        if (enemy == null) return null;

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
