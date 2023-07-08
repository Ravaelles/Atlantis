package atlantis.combat.managers;

import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.micro.managers.DanceAfterShoot;
import atlantis.combat.micro.managers.StopAndShoot;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.running.ShouldStopRunning;
import atlantis.terran.repair.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.interrupt.DontDisturbInterrupt;
import atlantis.units.managers.Manager;

public class CombatManagerMediumPriority extends Manager {

    public CombatManagerMediumPriority(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidEnemies.class,
            UnitBeingReparedManager.class,
            RetreatManager.class,
            AttackNearbyEnemies.class,
        };
    }

//    private boolean handledMediumPriority() {
//
//        // Avoid:
//        // - invisible units (Dark Templars)
//        // - close melee units (Zealots)
//        // - ranged units that can shoot at us (Dragoons)
//        // - defensive buildings (Cannons)
//        if (avoidEnemies.avoidEnemiesIfNeeded()) {
//            return true;
//        }
//
//        // If Near enemies would likely defeat us, retreat
////        if (RetreatManager.shouldRetreat(unit)) {
////            return true;
////        }
//
//        // Handle repair of mechanical units
//        if (unitBeingReparedManager.handleUnitShouldBeRepaired(unit)) {
//            return true;
//        }
//
//        if (retreatManager.handleRetreat()) {
//            return true;
//        }
//
//        if (AttackNearbyEnemies.handleAttackNearEnemyUnits(unit)) {
//            return true;
//        }
//    }
}

