package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.ImprovePerformanceHavingBigSupply;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.generic.unfreezer.Unfreezer;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.dancing.HoldToShoot;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.units.AUnit;
import atlantis.units.interrupt.ContinueAttackOrder;
import atlantis.units.interrupt.DontInterruptShootingUnits;
import atlantis.units.special.ManualOverrideManager;
import atlantis.units.special.PreventDoNothing;

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
            ManualOverrideManager.class,
            PreventDoNothing.class,
            Unfreezer.class,
            ImprovePerformanceHavingBigSupply.class,
            AvoidCriticalUnits.class,
            DanceAfterShoot.class,
            HoldToShoot.class,
            DontInterruptShootingUnits.class,
            ContinueAttackOrder.class,
            TransportUnits.class,
        };
    }
}

