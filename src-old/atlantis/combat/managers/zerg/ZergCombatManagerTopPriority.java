package atlantis.combat.managers.zerg;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.attack.protoss.ProtossAttackParamountUnitsInRange;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.combat.micro.avoid.buildings.protoss.ProtossCombatBuildingClose;
import atlantis.combat.micro.avoid.special.protoss.ProtossAvoidCriticalUnits;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.running.stop_running.ShouldStopRunning;
import atlantis.combat.state.AttackStateDeterminingManager;
import atlantis.units.AUnit;
import atlantis.units.interrupt.protoss.ProtossContinueAttack;
import atlantis.units.interrupt.protoss.ProtossForceContinueCriticalMeleeAttack;
import atlantis.units.special.idle.protoss.ProtossFixIdleUnits;
import atlantis.units.special.RemoveDeadUnitsManager;
import atlantis.units.special.ManualOverrideManager;
import atlantis.units.special.SpecialUnitsManager;
import atlantis.units.special.idle.protoss.ProtossFixInvalidTargets;

public class ZergCombatManagerTopPriority extends Manager {
    public ZergCombatManagerTopPriority(AUnit unit) {
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

            AvoidSpellsAndMines::new,
            SpecialUnitsManager::new,

            FixPerformanceForBigSupply::new,

            // === Crucial actions ===========================================

//            ProtossUnfreezer.class,
//            ProtossContinueUnfreeze.class,

            ProtossCombatBuildingClose::new,
            ProtossAvoidCriticalUnits::new,

            ProtossAttackParamountUnitsInRange::new,

//            ProtossForceRetreatDuringDefend.class,
            RetreatManager::new,

            // === Very important actions ====================================

            ProtossForceContinueCriticalMeleeAttack::new,
            ProtossContinueAttack::new,

            ProtossFixInvalidTargets::new,
            ProtossFixIdleUnits::new,

            ShouldStopRunning::new,

            // === Important actions ========================================

            ProtossAvoidEnemies::new,

            DanceAfterShoot::new,

            TransportUnits::new,
        };
    }
}

