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
import atlantis.combat.micro.generic.unfreezer.Unfreezer;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.missions.defend.protoss.ProtossForceStickToMainDuringDefend;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.running.stop_running.ShouldStopRunning;
import atlantis.combat.squad.positioning.KeepFormation;
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
            // === A ===========================================

            ManualOverrideManager.class,
            RemoveDeadUnitsManager.class,
            AttackStateDeterminingManager.class,

            AvoidSpellsAndMines.class,
            SpecialUnitsManager.class,

            DontIssueOrdersOneFrameAfterCommand.class,

            FixPerformanceForBigSupply.class,

            // === B ===========================================

            Unfreezer.class,

            AvoidCombatBuildingClose.class,
            AvoidCriticalUnits.class,

            HoldToShoot.class,
            ContinueShotAnimation.class,

            KeepFormation.class,

            RetreatManager.class,
            ShouldStopRunning.class,
//            ForceRunAfterShot.class,

            AttackParamountUnitsInRange.class,
            AttackTanksInRange.class,
//            ContinueShooting.class,

            FixInvalidTargets.class,
            FixIdleUnits.class,

            // === C ===========================================

            AvoidAsUndetected.class,
            AvoidEnemies.class,

            DanceAfterShoot.class,
//            ContinueCurrentAction.class,

            ProtossEarlyGame.class,

            ProtossCombatManager.class,
            TerranCombatManager.class,

            ProtossForceStickToMainDuringDefend.class,

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

