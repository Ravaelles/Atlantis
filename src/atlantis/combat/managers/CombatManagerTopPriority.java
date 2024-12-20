package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.avoid.ContinueRunning;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingClose;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.dancing.hold.HoldToShoot;
import atlantis.combat.micro.dancing.away.DanceAwayAsMelee;
import atlantis.combat.micro.early.protoss.ProtossEarlyGame;
import atlantis.combat.micro.generic.unfreezer.Unfreezer;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.squad.positioning.Cohesion;
import atlantis.units.AUnit;
import atlantis.units.interrupt.ContinueCurrentAction;
import atlantis.units.interrupt.ContinueShooting;
import atlantis.units.interrupt.ContinueShotAnimation;
import atlantis.units.special.FixIdleUnits;
import atlantis.units.special.RemoveDeadUnits;
import atlantis.units.special.ManualOverrideManager;
import atlantis.units.special.ums.FixInvalidTargets;

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
            RemoveDeadUnits.class,

            AvoidCombatBuildingClose.class,

            ContinueShotAnimation.class,

            Unfreezer.class,
//            DetectUnitsNotRunning.class,
            FixPerformanceForBigSupply.class,

            FixInvalidTargets.class,
            FixIdleUnits.class,

            ProtossCombatManager.class,
            TerranCombatManager.class,

            HoldToShoot.class,
            ContinueShooting.class,
            ContinueCurrentAction.class,

            AvoidCriticalUnits.class,

            ContinueRunning.class,

            ProtossEarlyGame.class,
//            AvoidCombatBuilding.class,

            Cohesion.class,

//            FixStoppedUnits.class,

            RetreatManager.class,

//            PreventDoNothing.class,
//            PreventAttackNull.class,
//            PreventAttackForTooLong.class,

            DanceAwayAsMelee.class,
            DanceAfterShoot.class,
//            ContinueShooting.class,
            TransportUnits.class,
        };
    }
}

