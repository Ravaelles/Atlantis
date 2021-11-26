package atlantis.combat.micro.avoid;

import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.micro.terran.TerranFirebat;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.targeting.ATargetingCrucial;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.Units;
import atlantis.util.We;

public class FightInsteadAvoid {

    protected final AUnit unit;
    protected final Units enemies;

    /**
     * Enemy units of different types that are dangerously close, extracted as variables for easier access
     */
    protected final AUnit defensiveBuilding;
    protected final AUnit invisibleDT;
    protected final AUnit invisibleCombatUnit;
    protected final AUnit lurker;
    protected final AUnit tankSieged;
    protected final AUnit tanks;
    protected final AUnit reaver;
    protected final AUnit vulture;
    protected final AUnit ranged;
    protected final AUnit melee;

    // =========================================================

    public FightInsteadAvoid(AUnit unit, Units enemies) {
        this.unit = unit;
        this.enemies = enemies;

        Selection selector = Select.from(enemies);
        invisibleDT = selector.clone().ofType(AUnitType.Protoss_Dark_Templar).effCloaked().first();
        invisibleCombatUnit = selector.clone().effCloaked().combatUnits().first();
        lurker = selector.clone().ofType(AUnitType.Zerg_Lurker).first();
        tankSieged = selector.clone().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode).first();
        tanks = selector.clone().tanks().first();
        vulture = selector.clone().ofType(AUnitType.Terran_Vulture).first();
        reaver = selector.clone().ofType(AUnitType.Protoss_Reaver).first();
        defensiveBuilding = selector.clone().buildings().first();
        ranged = selector.clone().ranged().first();
        melee = selector.clone().melee().first();
    }

    // =========================================================

    public boolean shouldFight() {
        if (enemies.isEmpty()) {
            return false;
        }

        if (dontFightInImportantCases()) {
            return false;
        }

        if (unit.mission().forcesUnitToFight(unit, enemies)) {
            return true;
        }

        // Workers
        if (unit.isWorker()) {
            return fightAsWorker(unit, enemies);
        }

        // Combat units
        else {
            if (fightInImportantCases()) {
//                System.err.println("Important case");
                return true;
            }

            return fightAsCombatUnit();
        }
    }

    // =========================================================

    protected boolean dontFightInImportantCases() {

        // Always avoid invisible combat units
        if (invisibleDT != null || invisibleCombatUnit != null) {
            return true;
        }

        return false;
    }

    protected boolean fightInImportantCases() {
        if (unit.isWorker()) {
            System.err.println("Worker in fightInImportantCases");
        }

        if (defensiveBuilding != null && fightBecauseWayTooManyUnitsNearby(unit)) {
            return true;
        }

        // Attacking critically important unit
        if (ATargetingCrucial.isCrucialUnit(unit.target())) {
            unit.setTooltip("Crucial!");
            return true;
        }

        if (forbidMeleeUnitsAbandoningCloseTargets(unit)) {
            return true;
        }

        if (forbidAntiAirAbandoningCloseTargets(unit)) {
            return true;
        }

        return false;
    }

    // RANGED
    protected boolean fightAsRangedUnit() {
        if (melee != null) {
            return false;
        }

        if (vulture != null) {
            return true;
        }

        if (ranged != null) {

            if (unit.isTank() && unit.lastUnderAttackLessThanAgo(30 * 3)) {
                return false;
            }

            // Dragoon faster than Marines, can outrun them
            if (unit.isQuickerOrSameSpeedAs(enemies) && unit.hasBiggerRangeThan(enemies)) {

                // If needs to wait before next attack
//                return unit.cooldownRemaining() <= 3 || unit.isJustShooting() || unit.lastUnderAttackMoreThanAgo(200);
                return unit.woundPercent() <= 40 && unit.lastUnderAttackMoreThanAgo(30 * 8);
            }

            // Dragoon slower than Vultures, cannot outrun them
            else {
//                AUnit main = Select.mainBase();
//                if (main != null && main.distToLessThan(unit, 9)) {
//                    return true;
//                }

//                return unit.hpPercent() > 50 && unit.getCooldownCurrent() <= 2 && unit.hasWeaponRange(ranged, 0);
                return false;
            }
        }

        return false;
    }

    // MELEE
    protected boolean fightAsMeleeUnit() {
        if (invisibleDT != null || invisibleCombatUnit != null) {
            return false;
        }

        return !RetreatManager.shouldRetreat(unit, enemies);
    }

    protected boolean fightAsCombatUnit() {
        if (fightBecauseWayTooManyUnitsNearby(unit)) {
            return true;
        }

        if (enemies.onlyRanged() && ACombatEvaluator.isSituationFavorable(unit)) {
            return true;
        }

//        if (unit.isSquadScout()) {
//            return Select.our().inRadius(3, unit).atLeast(3);
//        }

        if (lurker != null && (!lurker.isBurrowed() || lurker.isDetected())) {
            return true;
        }

        if (tankSieged != null || tanks != null) {
            return true;
        }

        if (defensiveBuilding != null) {
            return unit.mission().allowsToAttackDefensiveBuildings(unit, defensiveBuilding);
        }

        if (unit.isMelee()) {
            return fightAsMeleeUnit();
        } else {
            return fightAsRangedUnit();
        }
    }

    // =========================================================

    protected boolean forbidMeleeUnitsAbandoningCloseTargets(AUnit unit) {
        return unit.isMelee()
                && (!unit.isFirebat() || TerranFirebat.shouldContinueMeleeFighting(unit))
                && unit.enemiesNearby()
                    .canBeAttackedBy(unit, 3)
                    .inRadius(3, unit)
                    .isNotEmpty();
    }

    protected boolean forbidAntiAirAbandoningCloseTargets(AUnit unit) {
        return unit.isAirUnitAntiAir()
                && unit.enemiesNearby()
                .canBeAttackedBy(unit, 3)
                .isNotEmpty();
    }

    protected boolean fightBecauseWayTooManyUnitsNearby(AUnit unit) {
        int unitsNearby = Select.all().exclude(unit).inRadius(0.3, unit).count();
        int ourNearby = Select.our().exclude(unit).inRadius(0.3, unit).count();

        if (unit.mission() != null && unit.mission().isMissionAttack()) {
            if (We.terran()) {
                return unitsNearby >= 6 || (invisibleDT != null && unitsNearby >= 4)
                        || Select.ourCombatUnits().inRadius(10, unit).atLeast(25);
            }
            if (We.protoss()) {
                return unitsNearby >= 6 || (invisibleDT != null && unitsNearby >= 4)
                        || Select.ourCombatUnits().inRadius(10, unit).atLeast(10);
            }
            if (We.zerg()) {
                return unitsNearby >= 6 || (invisibleDT != null && unitsNearby >= 4)
                        || Select.ourCombatUnits().inRadius(10, unit).atLeast(20);
            }
        }

        return ourNearby >= 5 || unitsNearby >= 6;
    }

    protected boolean fightAsWorker(AUnit unit, Units enemies) {
        if (defensiveBuilding != null || lurker != null || reaver != null || tankSieged != null || melee != null || invisibleCombatUnit != null) {
            return false;
        }

        return unit.hpPercent() > 75 && unit.distToLessThan(Select.main(), 12);
    }

}
