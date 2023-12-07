package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.map.position.APosition;
import atlantis.terran.chokeblockers.ChokeBlockers;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import bwapi.TechType;

import java.util.HashMap;

public class TerranMedic extends Manager {
    public double BODY_BLOCK_POSITION_ERROR_MARGIN = 0.2;
    public double MAX_DIST_TO_ASSIGNMENT = 1.65;
    public double MIN_DIST_TO_ASSIGNMENT = 0.45;

    /**
     * Maximum allowed distance for a medic to heal wounded units that are not their assignment.
     * The idea is to disallow them to move away too much.
     */
    public int HEAL_OTHER_UNITS_MAX_DISTANCE = 10;

    /**
     * Specific units that medics should follow in order to heal them as fast as possible
     * when they get wounded.
     */
    private final HashMap<AUnit, AUnit> medicsToAssignments = new HashMap<>();
    private final HashMap<AUnit, AUnit> assignmentsToMedics = new HashMap<>();
    private final AUnit medic;

    // =========================================================

    public TerranMedic(AUnit medic) {
        super(medic);
        this.medic = medic;
    }

    // =========================================================

    @Override
    public boolean applies() {
        return unit.isMedic();
    }

//    @Override
//    protected Class<? extends Manager>[] managers() {
//        return new Class[]{
////            ChokeBlock.class,
//            AvoidEnemies.class,
////            UnitTooCloseToBunker.class,
//        };
//    }

    protected Manager handle() {
        Manager manager;

        manager = (new MedicChokeBlockMoveAway(unit)).invoke();
        if (manager != null) return manager;

        manager = (new MedicChokeBlock(unit)).invoke();
        if (manager != null) return manager;

//        if (medic.hp() <= 14 && AvoidEnemies.avoidEnemiesIfNeeded()) {
//            return true;
//        }

//        if (
//            unit.isMissionDefend() && !IsUnitNearChoke.check(unit, 0.5)
//            && unit.friendsNear().excludeMedics().groundUnits().inRadius(2, unit).atMost(1)
//        ) {
//            medic.setTooltip("BlockChoke");
//            return null;
//        }

        if (!medic.isIdle() && medic.lastActionLessThanAgo(8, Actions.HEAL)) {
            medic.setTooltip("hEaL");
            return usedManager(this);
        }

//        if (unblockChoke()) {
//            return true;
//        }

//        if (healCriticallyWoundedUnits()) {
//            return true;
//        }

        if (healMostWoundedInRange()) {
            return usedManager(this);
        }

        if (healAnyWoundedNear()) {
            return usedManager(this);
        }

        UnitTooCloseToBunker unitTooCloseToBunker = new UnitTooCloseToBunker(unit);
        if (unitTooCloseToBunker.invoke() != null) {
            return usedManager(unitTooCloseToBunker);
        }

//        if (unit.isMissionDefend() && !IsUnitNearChoke.check(unit, 4)) {
        if (Enemy.protoss() && bodyBlockMelee()) {
            return usedManager(this);
        }

        if (tooFarFromNearestInfantry()) {
            return usedManager(this);
        }

        // If there's no "real" infantry around, go to the nearest Marine, Firebat or Ghost.
        if (handleStickToAssignments()) {
            return usedManager(this);
        }
//        }

        return null;
    }

    // =========================================================

    private boolean bodyBlockMelee() {
        if (medic.cooldownRemaining() >= 2) return false;

        if (medic.friendsInRadiusCount(1.2) <= 0) return false;

        Selection meleeEnemies = medic.enemiesNear().melee().inRadius(2.3, medic);
        if (meleeEnemies.count() == 0 || meleeEnemies.count() >= 2) return false;

        AUnit nearestFriend = medic.friendsNear()
            .inRadius(4, medic)
            .excludeTypes(AUnitType.Terran_Medic)
            .notBeingHealed()
            .nearestTo(medic);

        if (nearestFriend == null) return false;

        AUnit nearestEnemy = meleeEnemies.visibleOnMap().inRadius(4, medic).nearestTo(medic);
        if (nearestEnemy == null) return false;

        APosition enemyTarget = nearestEnemy.hasTargetPosition()
            ? nearestEnemy.targetPosition() : nearestFriend.position();
        APosition desiredPosition = enemyTarget.translateTilesTowards(0.4, nearestEnemy);
        if (medic.distToMoreThan(desiredPosition, BODY_BLOCK_POSITION_ERROR_MARGIN) || medic.isIdle()) {
            return medic.move(desiredPosition, Actions.MOVE_MACRO, "Block", false);
        }

        return false;
    }

//    private  boolean unblockChoke() {
//        AChoke choke = Chokes.nearestChoke();
//
//        // We're possibly blocking the choke
//        if (choke != null && choke.width() <= 3.8 && choke.distToLessThan(medic, choke.width() + 1)) {
//            AUnit nearestUnit = Select.ourCombatUnits().excludeTypes(AUnitType.Terran_Medic).nearestTo(unit);
//            if (nearestUnit != null && nearestUnit.distToLessThan(medic, 0.5)) {
//                return medic.moveAwayFrom(nearestUnit, 0.2, "MoveBitch"); // Get out of the way
//            }
//        }
//
//        return false;
//    }

    private boolean tooFarFromNearestInfantry() {
        AUnit infantry = Select.ourTerranInfantryWithoutMedics().nearestTo(medic);
        if (infantry != null && infantry.distToMoreThan(medic, 4)) {
            return medic.move(infantry, Actions.MOVE_FOCUS, "SemperFi", false);
        }

//        if (infantry == null) {
//            if (AvoidEnemies.avoidEnemiesIfNeeded()) {
//                return true;
//            }
//        }

        return false;
    }

    private void healUnit(AUnit unitToHeal) {
        if (medic != null && unitToHeal != null && !unitToHeal.equals(medic.target())) {
            medic.useTech(TechType.Healing, unitToHeal);
            medic.setTooltipTactical("Heal");
        }
    }

    private AUnit medicAssignment() {
        AUnit assignment = medicsToAssignments.get(medic);

        if (assignment != null && !assignment.isAlive()) {
            removeAssignment(assignment);
            assignment = null;
        }

        if (assignment == null) {
            assignment = createMedicAssignment();
        }

        return assignment;
    }

    private AUnit createMedicAssignment() {
        AUnit assignment;
        Selection inSquadSelector = Select.from(medic.squad()).inRadius(20, medic);

        // =========================================================
        // Firebats

        assignment = inSquadSelector.clone().ofType(AUnitType.Terran_Firebat).randomWithSeed(medic.id());
        if (assignment != null) {
            addMedicAssignment(assignment);
            return assignment;
        }

        // =========================================================
        // Infantry without any medics assigned

        assignment = inSquadSelector.clone()
            .terranInfantryWithoutMedics()
            .exclude(assignmentsToMedics.keySet())
            .randomWithSeed(medic.id());
        if (assignment != null) {
            addMedicAssignment(assignment);
            return assignment;
        }

        // =========================================================
        // Infantry even if already a medic is assigned

        assignment = inSquadSelector.clone()
            .terranInfantryWithoutMedics()
            .randomWithSeed(medic.id());
        if (assignment != null) {
            addMedicAssignment(assignment);
            return assignment;
        }

        return null;
    }

    private void addMedicAssignment(AUnit assignment) {
        medicsToAssignments.put(medic, assignment);
        assignmentsToMedics.put(assignment, medic);
        medic.setTooltipTactical("NewAssignment");
    }

    private void removeAssignment(AUnit assignment) {
        medicsToAssignments.remove(medic);
        assignmentsToMedics.remove(assignment);
    }

    private boolean handleStickToAssignments() {
        if (medic.nearestOurTankDist() < 1) {
            return medic.moveAwayFrom(medic.nearestOurTank(), 0.5, Actions.MOVE_SPACE, "Space4Tank");
        }

        AUnit assignment = medicAssignment();

        if (assignment != null && assignment.isAlive()) {
//            APainter.paintLine(medic, assignment, Color.White);

            double dist = assignment.distTo(medic);

            if (dist > MAX_DIST_TO_ASSIGNMENT) {
                return medic.move(assignment.position(), Actions.MOVE_FOLLOW, "Stick", false);
            }
            else if (dist <= MIN_DIST_TO_ASSIGNMENT) {
                return medic.moveAwayFrom(assignment.position(), 0.3, Actions.MOVE_FORMATION, "Separate");
            }
        }

        return false;
    }

//    private boolean healCriticallyWoundedUnits() {
//        if (medic.energy() < 2) return false;
//
//        AUnit nearestWoundedInfantry = allowedToBeHealed()
//            .criticallyWounded()
//            .inRadius(HEAL_OTHER_UNITS_MAX_DISTANCE, medic)
//            .nearestTo(medic);
//
//        // =========================================================
//        // If there's a wounded unit, heal it.
//
//        if (nearestWoundedInfantry != null) {
//            healUnit(nearestWoundedInfantry);
//            return true;
//        }
//
//        return false;
//    }

    private boolean healMostWoundedInRange() {
        if (!medic.energy(5)) return false;

        if (medic.lastActionLessThanAgo(10, Actions.HEAL)) return false;

        AUnit nearestWoundedInfantry = allowedToBeHealed()
            .notHavingHp(19)
            .inRadius(1.99, medic)
            .sortByHealth()
            .first();

        // =========================================================
        // If there's a wounded unit, heal it.

        if (nearestWoundedInfantry != null) {
            healUnit(nearestWoundedInfantry);
            medic.setTooltip("MostWounded");
            return true;
        }

        return false;
    }

    private Selection allowedToBeHealed() {
        return Select.our()
            .organic()
            .exclude(medic)
            .exclude(medicsToAssignments.values()) // Only heal units that have no medics assigned
            .notBeingHealed();
    }

    private boolean healAnyWoundedNear() {
        if (!medic.energy(5)) return false;

        Selection potentialTargets = Select.our()
            .organic()
            .wounded()
            .inRadius(HEAL_OTHER_UNITS_MAX_DISTANCE, medic)
            .exclude(medic);

        AUnit target = potentialTargets
            .excludeRunning()
            .notBeingHealed()
            .nearestTo(medic);

        if (target == null) {
            target = potentialTargets.nearestTo(medic);
        }

        // =========================================================
        // If there's a wounded unit, heal it.

        if (target != null) {
            healUnit(target);
            medic.setTooltip("AnyWounded");
            return true;
        }

        return false;
    }

}
