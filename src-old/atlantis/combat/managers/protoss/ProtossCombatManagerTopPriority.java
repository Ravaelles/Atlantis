package atlantis.combat.managers.protoss;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.attack.protoss.ProtossAttackParamountUnitsInRange;
import atlantis.combat.micro.attack.tanks.ProtossAttackTanksInRange;
import atlantis.combat.micro.attack.tanks.ProtossAttackTanksNearby;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.combat.micro.avoid.buildings.protoss.ProtossCombatBuildingClose;
import atlantis.combat.micro.avoid.special.protoss.ProtossAvoidCriticalUnits;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.early.protoss.ProtossEarlyGame;
import atlantis.combat.micro.generic.unfreezer.ProtossContinueUnfreeze;
import atlantis.combat.micro.generic.unfreezer.ProtossUnfreezer;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.protoss.ProtossForceRetreatDuringDefend;
import atlantis.combat.retreating.protoss.ProtossRetreat;
import atlantis.combat.running.low_eval.ProtossLowEval;
import atlantis.combat.running.stop_running.protoss.ProtossShouldStopRunning;
import atlantis.combat.squad.positioning.protoss.cluster.ProtossForceCluster;
import atlantis.combat.squad.positioning.protoss.formations.ProtossFormation;
import atlantis.combat.state.AttackStateDeterminingManager;
import atlantis.protoss.dragoon.DragoonAttackVultureInRange;
import atlantis.protoss.dt.DarkTemplar;
import atlantis.units.AUnit;
import atlantis.units.interrupt.protoss.ProtossContinueAttack;
import atlantis.units.interrupt.protoss.ProtossForceContinueCriticalMeleeAttack;
import atlantis.units.special.ManualOverrideManager;
import atlantis.units.special.RemoveDeadUnitsManager;
import atlantis.units.special.SpecialUnitsManager;
import atlantis.units.special.idle.protoss.ProtossFixIdleUnits;
import atlantis.units.special.idle.protoss.ProtossFixInvalidTargets;

public class ProtossCombatManagerTopPriority extends Manager {
    public ProtossCombatManagerTopPriority(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            // === Non-actions ===============================================

            ManualOverrideManager::new,
            RemoveDeadUnitsManager::new,
            AttackStateDeterminingManager::new,

            FixPerformanceForBigSupply::new,
            ProtossPreventIssuingTooRapidCommands::new,

            AvoidSpellsAndMines::new,

            SpecialUnitsManager::new,

            // === Crucial actions ===========================================

            ProtossUnfreezer::new,
            ProtossContinueUnfreeze::new,

            ProtossCombatBuildingClose::new,
            ProtossAvoidCriticalUnits::new,

            ProtossAttackParamountUnitsInRange::new,
            ProtossForceContinueCriticalMeleeAttack::new,

            ProtossForceRetreatDuringDefend::new,
            ProtossRetreat::new,

            ProtossAttackTanksInRange::new,

            // === Important actions ====================================

            ProtossAvoidEnemies::new,

            ProtossForceFight::new,
            ProtossLowEval::new,

            ProtossContinueAttack::new,

            ProtossFormation::new,

            DragoonAttackVultureInRange::new,
            ProtossAttackTanksNearby::new,

            ProtossEarlyGame::new,

            ProtossForceCluster::new,

            ProtossFixInvalidTargets::new,
            ProtossFixIdleUnits::new,

            ProtossShouldStopRunning::new,

            DarkTemplar::new,

//            ProtossTopCombatManager.class,

//            ContinueShooting.class,

            // === Important actions ========================================

            DanceAfterShoot::new,

            ProtossCombatManager::new,

            TransportUnits::new,
        };
    }
}

