package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.dancing.HoldToShoot;
import atlantis.combat.micro.generic.unfreezer.Unfreezer;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.RetreatManager;
import atlantis.units.AUnit;
import atlantis.units.fix.PreventAttackNull;
import atlantis.units.fix.PreventAttackForTooLong;
import atlantis.units.interrupt.ContinueCurrentAction;
import atlantis.units.special.FixInvalidUnits;
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
            FixInvalidUnits.class,
            FixPerformanceForBigSupply.class,
            ManualOverrideManager.class,

            Unfreezer.class,
            
            RetreatManager.class,

            PreventDoNothing.class,
            PreventAttackNull.class,
            PreventAttackForTooLong.class,

            ContinueCurrentAction.class,

            DanceAfterShoot.class,
//            ContinueShooting.class,
            HoldToShoot.class,
            AvoidCriticalUnits.class,
            TransportUnits.class,
        };
    }
}

