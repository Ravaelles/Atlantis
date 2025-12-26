package atlantis.combat.managers.terran;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.attack.terran.TerranAttackParamountUnitsInRange;
import atlantis.combat.micro.avoid.buildings.terran.TerranAvoidCombatBuildingClose;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.micro.avoid.special.terran.TerranAvoidCriticalUnits;
import atlantis.combat.micro.avoid.terran.TerranAvoidEnemies;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.terran.TerranRetreatManager;
import atlantis.combat.running.stop_running.terran.TerranShouldStopRunning;
import atlantis.combat.state.AttackStateDeterminingManager;
import atlantis.units.AUnit;
import atlantis.units.interrupt.terran.TerranContinueAttack;
import atlantis.units.special.ManualOverrideManager;
import atlantis.units.special.RemoveDeadUnitsManager;
import atlantis.units.special.SpecialUnitsManager;

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

//            ProtossUnfreezer.class,
//            ProtossContinueUnfreeze.class,

            TerranAvoidCombatBuildingClose.class,
            TerranAvoidCriticalUnits.class,

            TerranAttackParamountUnitsInRange.class,

            TerranRetreatManager.class,

            // === Very important actions ====================================

            TerranAvoidEnemies.class,

            TerranContinueAttack.class,

//            ProtossFixInvalidTargets.class,
//            ProtossFixIdleUnits.class,

            TerranShouldStopRunning.class,

            // === Important actions ========================================

//            DanceAfterShoot.class,

            TerranCombatManager.class,

            TransportUnits.class,
        };
    }
}

