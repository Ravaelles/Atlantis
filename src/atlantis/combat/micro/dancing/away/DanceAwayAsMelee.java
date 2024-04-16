package atlantis.combat.micro.dancing.away;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.dancing.HoldToShoot;
import atlantis.combat.micro.generic.unfreezer.Unfreezer;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.RetreatManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.fix.PreventAttackForTooLong;
import atlantis.units.fix.PreventAttackNull;
import atlantis.units.fix.PreventDoNothing;
import atlantis.units.interrupt.ContinueCurrentAction;
import atlantis.units.special.FixInvalidUnits;
import atlantis.units.special.ManualOverrideManager;

public class DanceAwayAsMelee extends Manager {
    public DanceAwayAsMelee(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMelee();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DanceAwayAsZealot.class,
        };
    }
}
