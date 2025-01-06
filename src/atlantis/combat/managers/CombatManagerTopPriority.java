package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.attack.AttackParamountUnitsInRange;
import atlantis.combat.micro.attack.expansion.OverrideAndAttackEnemyExpansion;
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
import atlantis.units.interrupt.ContinueShooting;
import atlantis.units.interrupt.ContinueShotAnimation;
import atlantis.units.special.FixIdleUnits;
import atlantis.units.special.RemoveDeadUnitsManager;
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
            RemoveDeadUnitsManager.class,

            FixPerformanceForBigSupply.class,

            Unfreezer.class,

            AvoidCombatBuildingClose.class,

            ContinueShotAnimation.class,

            DanceAwayAsMelee.class,
            DanceAfterShoot.class,

            HoldToShoot.class,
            ContinueShooting.class,
//            ContinueCurrentAction.class,

            AttackParamountUnitsInRange.class,
            ProtossEarlyGame.class,

            OverrideAndAttackEnemyExpansion.class,

            ProtossCombatManager.class,
            TerranCombatManager.class,

            AvoidCriticalUnits.class,

            FixInvalidTargets.class,
            FixIdleUnits.class,

            ContinueRunning.class,

//            AvoidCombatBuilding.class,

            Cohesion.class,

//            FixStoppedUnits.class,

            RetreatManager.class,

//            PreventDoNothing.class,
//            PreventAttackNull.class,
//            PreventAttackForTooLong.class,

//            ContinueShooting.class,
            TransportUnits.class,
        };
    }
}

