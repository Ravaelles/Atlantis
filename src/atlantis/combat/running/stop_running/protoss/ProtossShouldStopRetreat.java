package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossShouldStopRetreat extends Manager {
    public ProtossShouldStopRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isRetreating()) return false;
//        if (unit.lastStartedRunningLessThanAgo(20)) return false;
        if (unit.lastStartedRetreatingAgo() <= 30 * 3) return false;
        if (unit.leaderIsRetreating() && unit.squadLeader().lastStartedRetreatingAgo() <= 30 * 3) return false;

        return noEnemiesNear()
            || unit.eval() >= 1.3
            || (unit.cooldown() <= 7 && (unit.distToCannon() <= 1.9 || unit.distToBase() <= 5));
    }

    private boolean noEnemiesNear() {
        return unit.enemiesNear().combatUnits().canAttack(unit, 7).empty();
    }

    @Override
    public Manager handle() {
        if (stopRetreating(unit)) {
            for (AUnit friend : unit.squad().list()) {
                stopRetreating(friend);
            }
            return usedManager(this);
        }

        return null;
    }

    private boolean stopRetreating(AUnit friend) {
        friend.runningManager().stopRunning();
        if (friend.isAction(Actions.RUN_RETREAT)) {
            friend.stop("StopRetreat");
        }

        if (unit.hasAnyWeapon()) {
            if ((new AttackNearbyEnemies(friend)).invokedFrom(this)) return true;
        }

        return false;
    }
}
