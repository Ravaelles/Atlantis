package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.ImprovePerformanceHavingBigSupply;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.dancing.HoldToShoot;
import atlantis.combat.micro.generic.unfreezer.Unfreezer;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.units.AUnit;
import atlantis.units.fix.PreventAttackNull;
import atlantis.units.fix.PreventAttackTooLong;
import atlantis.units.interrupt.ContinueCurrentAction;
import atlantis.units.special.ManualOverrideManager;
import atlantis.units.fix.PreventDoNothing;

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
            ImprovePerformanceHavingBigSupply.class,
            ManualOverrideManager.class,

            PreventDoNothing.class,
            PreventAttackNull.class,
            PreventAttackTooLong.class,

            ContinueCurrentAction.class,
            
            Unfreezer.class,
            DanceAfterShoot.class,
//            ContinueShooting.class,
            HoldToShoot.class,
            AvoidCriticalUnits.class,
            TransportUnits.class,
        };
    }
}

