package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.ImprovePerformanceHavingBigSupply;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.generic.Unfreezer;
import atlantis.combat.micro.managers.DanceAfterShoot;
import atlantis.combat.micro.managers.HoldToShoot;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.running.ShouldStopRunning;
import atlantis.units.AUnit;
import atlantis.units.interrupt.DontInterruptShootingUnits;
import atlantis.units.interrupt.DontInterruptStartedAttacks;

public class CombatManagerTopPriority extends Manager {
    public CombatManagerTopPriority(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidCriticalUnits.class,
            Unfreezer.class,
            ImprovePerformanceHavingBigSupply.class,
            DontInterruptShootingUnits.class,
            HoldToShoot.class,
            DanceAfterShoot.class,
            TransportUnits.class,
            ShouldStopRunning.class,
        };
    }
}

