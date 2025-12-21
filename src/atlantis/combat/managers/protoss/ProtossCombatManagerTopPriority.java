package atlantis.combat.managers.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.FixPerformanceForBigSupply;
import atlantis.combat.micro.attack.AttackParamountUnitsInRange;
import atlantis.combat.micro.attack.tanks.ProtossAttackTanksInRange;
import atlantis.combat.micro.attack.tanks.ProtossAttackTanksNearby;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingClose;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.micro.dancing.DanceAfterShoot;
import atlantis.combat.micro.early.protoss.ProtossEarlyGame;
import atlantis.combat.micro.generic.unfreezer.ContinueUnfreeze;
import atlantis.combat.micro.generic.unfreezer.Unfreezer;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.protoss.ProtossForceRetreatDuringDefend;
import atlantis.combat.retreating.protoss.ProtossRetreat;
import atlantis.combat.running.stop_running.protoss.ProtossShouldStopRunning;
import atlantis.combat.squad.positioning.protoss.formations.ProtossFormation;
import atlantis.combat.state.AttackStateDeterminingManager;
import atlantis.protoss.dragoon.DragoonAttackVultureInRange;
import atlantis.protoss.dt.DarkTemplar;
import atlantis.units.AUnit;
import atlantis.units.interrupt.ContinueAttack;
import atlantis.units.interrupt.ForceContinueCriticalMeleeAttack;
import atlantis.units.special.ManualOverrideManager;
import atlantis.units.special.RemoveDeadUnitsManager;
import atlantis.units.special.SpecialUnitsManager;
import atlantis.units.special.idle.FixIdleUnits;
import atlantis.units.special.idle.FixInvalidTargets;

public class ProtossCombatManagerTopPriority extends Manager {
    public ProtossCombatManagerTopPriority(AUnit unit) {
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

            FixPerformanceForBigSupply.class,
            ProtossPreventIssuingTooRapidCommands.class,

            AvoidSpellsAndMines.class,

            SpecialUnitsManager.class,

            // === Crucial actions ===========================================

            Unfreezer.class,
            ContinueUnfreeze.class,

            AvoidCombatBuildingClose.class,

            AvoidCriticalUnits.class,

            AttackParamountUnitsInRange.class,

            ProtossForceRetreatDuringDefend.class,
            ProtossRetreat.class,

            ProtossAttackTanksInRange.class,

            // === Very important actions ====================================

            ProtossFormation.class,

            ForceContinueCriticalMeleeAttack.class,
            ContinueAttack.class,

            DragoonAttackVultureInRange.class,
            ProtossAttackTanksNearby.class,

            ProtossEarlyGame.class,

            FixInvalidTargets.class,
            FixIdleUnits.class,

            ProtossShouldStopRunning.class,

            DarkTemplar.class,

//            ProtossTopCombatManager.class,

//            ContinueShooting.class,

            // === Important actions ========================================

            AvoidEnemies.class,

            DanceAfterShoot.class,

            ProtossCombatManager.class,

            TransportUnits.class,
        };
    }
}

