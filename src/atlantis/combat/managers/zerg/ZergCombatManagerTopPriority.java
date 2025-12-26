package atlantis.combat.managers.zerg;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            // === Non-actions ===============================================

            ManualOverrideManager.class,
            RemoveDeadUnitsManager.class,
            AttackStateDeterminingManager.class,

            AvoidSpellsAndMines.class,
            SpecialUnitsManager.class,

            FixPerformanceForBigSupply.class,

            // === Crucial actions ===========================================

//            ProtossUnfreezer.class,
//            ProtossContinueUnfreeze.class,

            ProtossCombatBuildingClose.class,
            ProtossAvoidCriticalUnits.class,

            ProtossAttackParamountUnitsInRange.class,

//            ProtossForceRetreatDuringDefend.class,
            RetreatManager.class,

            // === Very important actions ====================================

            ProtossForceContinueCriticalMeleeAttack.class,
            ProtossContinueAttack.class,

            ProtossFixInvalidTargets.class,
            ProtossFixIdleUnits.class,

            ShouldStopRunning.class,

            // === Important actions ========================================

            ProtossAvoidEnemies.class,

            DanceAfterShoot.class,

            TransportUnits.class,
        };
    }
}

