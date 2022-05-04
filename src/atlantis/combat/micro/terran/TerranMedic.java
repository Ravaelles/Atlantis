package atlantis.combat.micro.terran;

import atlantis.combat.micro.Microable;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.terran.medic.BodyBlock;
import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import bwapi.Color;
import bwapi.TechType;

import java.util.HashMap;

public class TerranMedic extends Microable {

    public static double BODY_BLOCK_POSITION_ERROR_MARGIN = 0.2;
    public static double MIN_DIST_TO_ASSIGNMENT = 0.45;

    /**
     * Maximum allowed distance for a medic to heal wounded units that are not their assignment.
     * The idea is to disallow them to move away too much.
     */
    public static int HEAL_OTHER_UNITS_MAX_DISTANCE = 10;

    /**
     * Specific units that medics should follow in order to heal them as fast as possible
     * when they get wounded.
     */
    private static final HashMap<AUnit, AUnit> medicsToAssignments = new HashMap<>();
    private static final HashMap<AUnit, AUnit> assignmentsToMedics = new HashMap<>();

    public Class[] macroManagers = new Class[] {
        BodyBlock.class,
    };

    // =========================================================

    public boolean update(AUnit medic) {
        if (medic.hp() <= 17 && AvoidEnemies.avoidEnemiesIfNeeded(medic)) {
            return true;
        }

        if (medic.lastActionLessThanAgo(5, Actions.HEAL)) {
            return true;
        }

//        if (unblockChoke(medic)) {
//            return true;
//        }

//        if (healCriticallyWoundedUnits(medic)) {
//            return true;
//        }

        if (healMostWoundedInRange(medic)) {
            return true;
        }

        if (Enemy.protoss() && bodyBlockMelee(medic)) {
            return true;
        }

        if (healAnyWoundedNear(medic)) {
            return true;
        }

        if (tooFarFromNearestInfantry(medic)) {
            return true;
        }

        // If there's no "real" infantry around, go to the nearest Marine, Firebat or Ghost.
        return handleStickToAssignments(medic);
    }

    // =========================================================

    private static boolean bodyBlockMelee(AUnit medic) {
        Selection meleeEnemies = medic.enemiesNear().melee().inRadius(6, medic);
//        if (meleeEnemies.count() == 0 || meleeEnemies.count() >= 3) {
        if (meleeEnemies.count() == 0) {
            return false;
        }

        AUnit nearestFriend = medic.friendsNear().excludeTypes(AUnitType.Terran_Medic).nearestTo(medic);
        AUnit nearestEnemy = meleeEnemies.inRadius(4, medic).nearestTo(medic);
        if (nearestEnemy == null || nearestFriend == null) {
            return false;
        }

        APosition desiredPosition = nearestFriend.translateTilesTowards(0.4, nearestEnemy);
        if (medic.distToMoreThan(desiredPosition, BODY_BLOCK_POSITION_ERROR_MARGIN) || medic.isIdle()) {
            return medic.move(desiredPosition, Actions.MOVE_MACRO, "Block", false);
        }

        return false;
    }

//    private static boolean unblockChoke(AUnit medic) {
//        AChoke choke = Chokes.nearestChoke(medic);
//
//        // We're possibly blocking the choke
//        if (choke != null && choke.width() <= 3.8 && choke.distToLessThan(medic, choke.width() + 1)) {
//            AUnit nearestUnit = Select.ourCombatUnits().excludeTypes(AUnitType.Terran_Medic).nearestTo(medic);
//            if (nearestUnit != null && nearestUnit.distToLessThan(medic, 0.5)) {
//                return medic.moveAwayFrom(nearestUnit, 0.2, "MoveBitch"); // Get out of the way
//            }
//        }
//
//        return false;
//    }

    private static boolean tooFarFromNearestInfantry(AUnit medic) {
        AUnit infantry = Select.ourTerranInfantryWithoutMedics().nearestTo(medic);
        if (infantry != null && infantry.distToMoreThan(medic, 4)) {
            return medic.move(infantry, Actions.MOVE_FOCUS, "SemperFi", false);
        }

        if (infantry == null) {
            if (AvoidEnemies.avoidEnemiesIfNeeded(medic)) {
                return true;
            }
        }

        return false;
    }

    private static void healUnit(AUnit medic, AUnit unitToHeal) {
        if (medic != null && unitToHeal != null && !unitToHeal.equals(medic.target())) {
            medic.useTech(TechType.Healing, unitToHeal);
            medic.setTooltipTactical("Heal");
        }
    }

    private static AUnit medicAssignment(AUnit medic) {
        AUnit assignment = medicsToAssignments.get(medic);

        if (assignment != null && !assignment.isAlive()) {
            removeAssignment(medic, assignment);
            assignment = null;
        }

        if (assignment == null) {
            assignment = createMedicAssignment(medic);
        }

        return assignment;
    }

    private static AUnit createMedicAssignment(AUnit medic) {
        AUnit assignment;
        Selection inSquadSelector = Select.from(medic.squad()).inRadius(20, medic);

        // =========================================================
        // Firebats

        assignment = inSquadSelector.clone().ofType(AUnitType.Terran_Firebat).randomWithSeed(medic.id());
        if (assignment != null) {
            addMedicAssignment(medic, assignment);
            return assignment;
        }

        // =========================================================
        // Infantry without any medics assigned

        assignment = inSquadSelector.clone()
                .terranInfantryWithoutMedics()
                .exclude(assignmentsToMedics.keySet())
                .randomWithSeed(medic.id());
        if (assignment != null) {
            addMedicAssignment(medic, assignment);
            return assignment;
        }

        // =========================================================
        // Infantry even if already a medic is assigned

        assignment = inSquadSelector.clone()
                .terranInfantryWithoutMedics()
                .randomWithSeed(medic.id());
        if (assignment != null) {
            addMedicAssignment(medic, assignment);
            return assignment;
        }

        return null;
    }

    private static void addMedicAssignment(AUnit medic, AUnit assignment) {
        medicsToAssignments.put(medic, assignment);
        assignmentsToMedics.put(assignment, medic);
        medic.setTooltipTactical("NewAssignment");
    }

    private static void removeAssignment(AUnit medic, AUnit assignment) {
        medicsToAssignments.remove(medic);
        assignmentsToMedics.remove(assignment);
    }

    private static boolean handleStickToAssignments(AUnit medic) {
        AUnit assignment = medicAssignment(medic);

        if (assignment != null && assignment.isAlive()) {
            APainter.paintLine(medic, assignment, Color.White);

            double dist = assignment.distTo(medic);

            if (dist > 1.9) {
                return medic.move(assignment.position(), Actions.MOVE_FOLLOW, "Stick", false);
            }
            else if (dist <= MIN_DIST_TO_ASSIGNMENT) {
                return medic.moveAwayFrom(assignment.position(), 0.3, "Separate", Actions.MOVE_FORMATION);
            }
//            else if (medic.isMoving()) {
//                return medic.holdPosition("Ok");
//            }
        }
        
        return false;
    }

    private static boolean healCriticallyWoundedUnits(AUnit medic) {
        if (medic.energy() < 2) {
            return false;
        }

        AUnit nearestWoundedInfantry = Select.our()
                .organic()
                .criticallyWounded()
                .inRadius(HEAL_OTHER_UNITS_MAX_DISTANCE, medic)
                .exclude(medic)
                .nearestTo(medic);

        // =========================================================
        // If there's a wounded unit, heal it.

        if (nearestWoundedInfantry != null) {
            healUnit(medic, nearestWoundedInfantry);
            return true;
        }

        return false;
    }

    private static boolean healMostWoundedInRange(AUnit medic) {
        if (!medic.energy(5)) {
            return false;
        }

        AUnit nearestWoundedInfantry = Select.our()
                .organic()
                .notHavingHp(19)
                .inRadius(1.99, medic)
                .exclude(medic)
                .sortByHealth()
                .first();

        // =========================================================
        // If there's a wounded unit, heal it.

        if (nearestWoundedInfantry != null) {
            healUnit(medic, nearestWoundedInfantry);
            return true;
        }

        return false;
    }

    private static boolean healAnyWoundedNear(AUnit medic) {
        if (!medic.energy(5)) {
            return false;
        }

        AUnit nearestWoundedInfantry = Select.our()
                .organic()
                .wounded()
                .inRadius(HEAL_OTHER_UNITS_MAX_DISTANCE, medic)
                .exclude(medic)
                .nearestTo(medic);

        // =========================================================
        // If there's a wounded unit, heal it.

        if (nearestWoundedInfantry != null) {
            healUnit(medic, nearestWoundedInfantry);
            return true;
        }

        return false;
    }

}
