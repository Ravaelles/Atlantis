package atlantis.battle.scout;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.battle.avoid.protoss.ProtossAvoidEnemies;
import atlantis.battle.avoid.special.protoss.ProtossAvoidCriticalUnits;
import atlantis.map.scout.ScoutAvoidCombatBuildings;
import atlantis.map.scout.ScoutEnemyNaturalIfNotExisting;
import atlantis.map.scout.ScoutEnemyThird;
import atlantis.map.scout.ScoutHoldCourse;
import atlantis.map.scout.ScoutFreeBases;
import atlantis.map.scout.ScoutPotentialEnemyBases;
import atlantis.map.scout.ScoutPotentialTerranBases;
import atlantis.map.scout.ScoutSeparateFromCloseWorkers;
import atlantis.map.scout.ScoutTryFindingEnemy;
import atlantis.map.scout.ScoutUnexploredBasesNearEnemy;
import atlantis.map.scout.ScoutEnemyBasesWithoutBase;
import atlantis.map.scout.ScoutFallbackEnemyBases;
import atlantis.map.scout.ScoutTerminalAnchor;
import atlantis.map.scout.ScoutUnitManager;
import atlantis.map.scout.enemy.ScoutApproachEnemyBase;
import atlantis.map.scout.enemy.ScoutNearEnemyBase;
import atlantis.units.AUnit;

/**
 * The Combat half of {@link ScoutUnitManager}: for one scout unit, the scouting
 * policies from {@code atlantis.map.scout} plus the combat micro it needs.
 *
 * <p>This class used to be {@code atlantis.map.scout.ScoutManager} and listed
 * {@code ProtossAvoidEnemies} / {@code ProtossAvoidCriticalUnits} directly -
 * a Scouting → Combat dependency the boundary test had frozen. Moving it here
 * keeps the wiring in the layer that owns the behaviour, and Scouting only ever
 * sees the {@link ScoutUnitManager} interface.</p>
 */
public class ScoutUnitManagers implements ScoutUnitManager {

    @Override
    public Manager create(AUnit scout) {
        return new ScoutUnitManagerFor(scout);
    }

    // =========================================================

    private static class ScoutUnitManagerFor extends Manager {
        private ScoutUnitManagerFor(AUnit unit) {
            super(unit);
        }

        @Override
        public boolean applies() {
            return true;
        }

        @Override
        protected ManagerFactory[] managers() {
            return new ManagerFactory[]{
                ProtossAvoidEnemies::new,
                ScoutSeparateFromCloseEnemies::new,
                ScoutSeparateFromCloseWorkers::new,
                ProtossAvoidCriticalUnits::new,
                ScoutAvoidCombatBuildings::new,

                // Ride the in-flight course before any movement manager
                // re-evaluates (owner: alternating destinations walked the
                // early scout between bases and it never saw the enemy).
                // Avoids above still preempt every frame.
                ScoutHoldCourse::new,

                ScoutEnemyNaturalIfNotExisting::new,
                ScoutEnemyThird::new,

                ScoutTryFindingEnemy::new,

                ScoutPotentialTerranBases::new,
                ScoutNearEnemyBase::new,

                ScoutUnexploredBasesNearEnemy::new,
                ScoutPotentialEnemyBases::new,
                ScoutEnemyBasesWithoutBase::new,

                ScoutFreeBases::new,

                ScoutApproachEnemyBase::new,
                ScoutFallbackEnemyBases::new,
                ScoutTerminalAnchor::new,
            };
        }

        @Override
        protected Manager handle() {
            unit.setTooltipTactical("Scout...");

            if (unit.isRepairing()) return usedManager(this, "UhmRepairing");

            return handleSubmanagers();
        }
    }
}
