package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.attack.AttackParamountUnitsInRange;
import atlantis.combat.micro.attack.tanks.AttackTanksInRange;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingClose;
import atlantis.combat.micro.avoid.special.AvoidAsUndetected;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.dancing.hold.HoldToShoot;
import atlantis.combat.micro.early.protoss.ProtossEarlyGame;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.running.stop_running.ShouldStopRunning;
import atlantis.combat.squad.positioning.protoss.cluster.ProtossForceUnitsCloserToLeader;
import atlantis.combat.squad.positioning.protoss.formations.ProtossFormation;
import atlantis.combat.state.AttackStateDeterminingManager;
import atlantis.units.AUnit;
import atlantis.units.interrupt.ContinueShotAnimation;
import atlantis.units.special.FixIdleUnits;
import atlantis.units.special.RemoveDeadUnitsManager;
import atlantis.units.special.ManualOverrideManager;
import atlantis.units.special.SpecialUnitsManager;
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
            // === Non-actions ===============================================

            ManualOverrideManager.class,
            RemoveDeadUnitsManager.class,
            AttackStateDeterminingManager.class,

            AvoidSpellsAndMines.class,
            SpecialUnitsManager.class,

            DontIssueOrdersOneFrameAfterCommand.class,
            FixPerformanceForBigSupply.class,

            // === Crucial actions ===========================================

//            Unfreezer.class,

            AvoidCombatBuildingClose.class,
            AvoidCriticalUnits.class,

            AttackParamountUnitsInRange.class,

            // === Very important actions ====================================

            RetreatManager.class,

            HoldToShoot.class,
            ContinueShotAnimation.class,
//            ForceRunAfterShot.class,

            ProtossFormation.class,
            ProtossForceUnitsCloserToLeader.class,

            FixInvalidTargets.class,
            FixIdleUnits.class,

            ShouldStopRunning.class,

            ProtossTopCombatManager.class,

            AttackTanksInRange.class,
//            ContinueShooting.class,

            // === Important actions ========================================

            AvoidAsUndetected.class,
            AvoidEnemies.class,

            DanceAfterShoot.class,
//            ContinueCurrentAction.class,

            ProtossEarlyGame.class,

            ProtossCombatManager.class,
            TerranCombatManager.class,

//            ProtossForceStickToMainDuringDefend.class,

//            ContinueRunning.class,

//            AvoidCombatBuilding.class,

//            Cohesion.class,

//            FixStoppedUnits.class,

//            RetreatManager.class,

//            FixInvalidTargets.class,
//            FixIdleUnits.class,

//            PreventDoNothing.class,
//            PreventAttackNull.class,
//            PreventAttackForTooLong.class,

//            ContinueShooting.class,
            TransportUnits.class,
        };
    }
}

