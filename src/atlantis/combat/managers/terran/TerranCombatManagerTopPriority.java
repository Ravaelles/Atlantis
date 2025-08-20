package atlantis.combat.managers.terran;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.attack.AttackParamountUnitsInRange;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingClose;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.generic.unfreezer.ContinueUnfreeze;
import atlantis.combat.micro.generic.unfreezer.Unfreezer;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.running.stop_running.ShouldStopRunning;
import atlantis.combat.state.AttackStateDeterminingManager;
import atlantis.units.AUnit;
import atlantis.units.interrupt.ContinueAttack;
import atlantis.units.interrupt.ForceContinueCriticalMeleeAttack;
import atlantis.units.special.ManualOverrideManager;
import atlantis.units.special.RemoveDeadUnitsManager;
import atlantis.units.special.SpecialUnitsManager;
import atlantis.units.special.idle.FixIdleUnits;
import atlantis.units.special.idle.FixInvalidTargets;

public class TerranCombatManagerTopPriority extends Manager {
    public TerranCombatManagerTopPriority(AUnit unit) {
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

            Unfreezer.class,
            ContinueUnfreeze.class,

            AvoidCombatBuildingClose.class,
            AvoidCriticalUnits.class,

            AttackParamountUnitsInRange.class,

            RetreatManager.class,

            // === Very important actions ====================================

            ForceContinueCriticalMeleeAttack.class,
            ContinueAttack.class,

            FixInvalidTargets.class,
            FixIdleUnits.class,

            ShouldStopRunning.class,

            // === Important actions ========================================

            AvoidEnemies.class,

            DanceAfterShoot.class,

            TerranCombatManager.class,

            TransportUnits.class,
        };
    }
}

