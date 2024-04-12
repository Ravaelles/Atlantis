package atlantis.combat.micro.dancing.away;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DanceAwayAsMelee extends Manager {
    public DanceAwayAsMelee(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMelee()) return false;

        return appliesAsProtoss(unit);
    }

    private boolean appliesAsProtoss(AUnit unit) {
        int meleedEnemiesNear;

        return unit.cooldown() >= 8
            && (meleedEnemiesNear = unit.meleeEnemiesNearCount(1.5)) > 0
            && (
                unit.woundPercent() >= 40
                ||
                meleedEnemiesNear >= 2
        );
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
