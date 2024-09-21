package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.avoid.ContinueRunning;
import atlantis.combat.micro.avoid.DetectUnitsNotRunning;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingCriticallyClose;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.dancing.away.DanceAwayAsMelee;
import atlantis.combat.micro.generic.unfreezer.Unfreezer;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.RetreatManager;
import atlantis.units.AUnit;
import atlantis.units.interrupt.ContinueShooting;
import atlantis.units.special.FixIdleUnits;
import atlantis.units.special.FixInvalidUnits;
import atlantis.units.special.ManualOverrideManager;

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

            AvoidCombatBuildingCriticallyClose.class,
//            AvoidCombatBuilding.class,

            ContinueShooting.class,
            ContinueRunning.class,
//            PreventTooManyActions.class,

            DetectUnitsNotRunning.class,
            FixPerformanceForBigSupply.class,
            ManualOverrideManager.class,

//            ContinueCurrentAction.class,

            Unfreezer.class,
            FixIdleUnits.class,

            AvoidCriticalUnits.class,

            RetreatManager.class,

//            PreventDoNothing.class,
//            PreventAttackNull.class,
//            PreventAttackForTooLong.class,

            DanceAwayAsMelee.class,
            DanceAfterShoot.class,
//            ContinueShooting.class,
//            HoldToShoot.class,
            TransportUnits.class,
        };
    }
}

