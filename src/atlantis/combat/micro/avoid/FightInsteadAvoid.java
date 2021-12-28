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
import atlantis.util.A;
import atlantis.util.Enemy;
import atlantis.util.We;

public class FightInsteadAvoid {

    protected final AUnit unit;
    protected final Units enemies;
    protected final Selection enemiesSelection;

    /**
     * Enemy units of different types that are dangerously close, extracted as variables for easier access
     */
    protected final AUnit combatBuilding;
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
        this.enemiesSelection = Select.from(enemies);

        Selection selector = Select.from(enemies);
        invisibleDT = selector.clone().ofType(AUnitType.Protoss_Dark_Templar).effCloaked().first();
        invisibleCombatUnit = selector.clone().effCloaked().combatUnits().first();
        lurker = selector.clone().ofType(AUnitType.Zerg_Lurker).first();
        tankSieged = selector.clone().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode).first();
        tanks = selector.clone().tanks().first();
        vulture = selector.clone().ofType(AUnitType.Terran_Vulture).first();
        reaver = selector.clone().ofType(AUnitType.Protoss_Reaver).first();
        combatBuilding = selector.clone().buildings().first();
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
//            System.err.println("Mission forced to fight!");
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
            if (unit.isTank() && !unit.isSieged() && unit.lastAttackFrameMoreThanAgo(30 * 4)) {
                return true;
            }

            // Dragoon faster than Marines, can outrun them
            if (unit.isQuickerOrSameSpeedAs(enemies) && unit.hasBiggerRangeThan(enemies)) {
                return unit.woundPercent() <= 40 && unit.lastUnderAttackMoreThanAgo(30 * 8);
            }

            // Dragoon slower than Vultures, cannot outrun them
            else {
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

        return !RetreatManager.shouldRetreat(unit);
    }

    protected boolean fightAsCombatUnit() {
        if (fightBecauseWayTooManyUnitsNearby(unit)) {
            return true;
        }

        if (RetreatManager.shouldRetreat(unit)) {
            return false;
        }

        if (handleTerranInfantryShouldFight(unit)) {
            return true;
        }

//        if (combatBuilding != null && fightBecauseWayTooManyUnitsNearby(unit)) {
//            return true;
//        }

//        if (enemies.onlyRanged() && ACombatEvaluator.isSituationFavorable(unit)) {
//        if (enemies.onlyMelee() && ACombatEvaluator.isSituationFavorable(unit)) {
//            return true;
//        }

        if (lurker != null && (!lurker.isBurrowed() || lurker.isDetected())) {
            return true;
        }

//        if (tankSieged != null || tanks != null) {
//            return true;
//        }

        if (combatBuilding != null) {
            return unit.mission().allowsToAttackCombatBuildings(unit, combatBuilding);
        }

        if (unit.isMelee()) {
            return fightAsMeleeUnit();
        } else {
            return fightAsRangedUnit();
        }
    }

    // =========================================================

    private boolean handleTerranInfantryShouldFight(AUnit unit) {
        if (!unit.isTerranInfantry()) {
            return false;
        }

        int meleeEnemiesNearby = unit.enemiesNearby().melee().inRadius(1.5, unit).count();
        if (unit.hp() <= (Enemy.protoss() ? 18 : 11) * meleeEnemiesNearby) {
            return false;
        }
//        if (unit.hp() <= (Enemy.protoss() ? 18 : 11) && enemiesSelection.melee().atLeast((Enemy.protoss() ? 1 : 2))) {
//            return false;
//        }

        boolean medicNearby = unit.medicNearby();
        return medicNearby || (!unit.isWounded() && ranged == null);
    }

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

        if (combatBuilding != null) {
            return unit.mission().isMissionAttack()
                    && Select.ourCombatUnits().inRadius(6, unit).atLeast(10)
                    && ACombatEvaluator.advantagePercent(unit, 50)
                    && A.printErrorAndReturnTrue("Fight DEF building cuz stacked " + unit.nameWithId());
        }

        return ourNearby >= 5 || unitsNearby >= 6;
    }

    protected boolean fightAsWorker(AUnit unit, Units enemies) {
        if (combatBuilding != null || lurker != null || reaver != null || tankSieged != null || melee != null || invisibleCombatUnit != null) {
            return false;
        }

        return unit.hpPercent() > 75 && unit.distToLessThan(Select.main(), 12);
    }

}
