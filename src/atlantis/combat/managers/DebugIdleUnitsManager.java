package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.managers.DanceAfterShoot;
import atlantis.combat.micro.managers.StopAndShoot;
import atlantis.combat.squad.positioning.TooClustered;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.DontInterruptShootingUnits;

public class DebugIdleUnitsManager extends Manager {
    public DebugIdleUnitsManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.looksIdle() && !skip();
    }

    private boolean skip() {
        if (true) return true;

        if (unit.isTankSieged()) return true;
        if (unit.isFirebat()) return true;
        if (unit.isMedic()) return true;


        if (unit.isInfantry()
            && (
            unit.manager() instanceof TooClustered
                || unit.manager() instanceof DontInterruptShootingUnits
                || unit.manager() instanceof DanceAfterShoot
                || unit.manager() instanceof StopAndShoot
                || unit.action().equals(Actions.LOAD)
        )
        ) {
            return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
        System.out.println("@ " + A.now()
            + " - IDLE - "
            + unit + " / "
            + unit.manager() + " / "
            + unit.getLastCommand().getType()
        );

        if (unit.isActiveManager(AttackNearbyEnemies.class)) {
            return usedManager(this);
        }

        return null;
    }
}

