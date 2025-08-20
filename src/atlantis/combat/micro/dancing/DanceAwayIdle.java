package atlantis.combat.micro.dancing;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DanceAwayIdle extends Manager {
    public DanceAwayIdle(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        System.out.println(unit.id() + " / " + unit.action() + " / " + unit.lastCommandName() + " / " + unit.isStopped());
        return unit.isAction(Actions.MOVE_DANCE_AWAY)
            && !unit.isMoving()
            && (unit.isStopped() || unit.lastPositionChangedMoreThanAgo(1));
    }

    @Override
    public Manager handle() {
        AttackNearbyEnemies attackNearbyEnemies = new AttackNearbyEnemies(unit);
        if (attackNearbyEnemies.forceHandle() != null) {
//            System.out.println("@ " + unit + " - DANCE AWAY INACTIVE -> ATTACK NEARBY ENEMIES");
            return usedManager(attackNearbyEnemies, "DanceAwayIdleAttack");
        }

        return null;
    }
}
