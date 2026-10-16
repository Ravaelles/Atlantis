package atlantis.combat.managers.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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

            TerranAvoidCombatBuildingClose::new,
            TerranAvoidCriticalUnits::new,

            TerranAttackParamountUnitsInRange::new,

            TerranRetreatManager::new,

            // === Very important actions ====================================

            TerranAvoidEnemies::new,

            TerranContinueAttack::new,

//            ProtossFixInvalidTargets.class,
//            ProtossFixIdleUnits.class,

            TerranShouldStopRunning::new,

            // === Important actions ========================================

//            DanceAfterShoot.class,

            TerranCombatManager::new,

            TransportUnits::new,
        };
    }
}

