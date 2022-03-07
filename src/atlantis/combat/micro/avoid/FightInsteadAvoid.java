package atlantis.combat.micro.avoid;

import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.targeting.ATargetingCrucial;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
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
//        if (true) return false;

        if (enemies.isEmpty()) {
            return true;
        }

        if (dontFightInTopImportantCases()) {
            unit.addLog("DoNotFight");
            return false;
        }

        if (unit.mission().forcesUnitToFight(unit, enemies)) {
            unit.addLog("ForcedFight");
            return true;
        }

        // Workers
        if (unit.isWorker()) {
            return fightAsWorker(unit, enemies);
        }

        // Combat units
        else {
            if (fightInImportantCases()) {
                unit.addLog("FightImportant");
                return true;
            }

            return fightAsCombatUnit();
        }
    }

    // =========================================================

    protected boolean fightAsCombatUnit() {
        if (fightBecauseWayTooManyUnitsNear(unit)) {
            unit.addLog("FightStacked");
            return true;
        }

        if (RetreatManager.shouldRetreat(unit)) {
            if (finishOffAlmostDeadTarget(unit)) {
                unit.addLog("FatalityTo" + unit.target().type());
                return true;
            }

            if (unit.isRanged() && ranged == null) {
                unit.addLog("FightRanged");
                return false;
            } else {
                unit.setTooltip("Retreat", true);
                unit.addLog("Retreat");
                return false;
            }
        }

        if (handleTerranInfantryShouldFight(unit)) {
            unit.addLog("InfantryFight");
            return true;
        }

//        if (combatBuilding != null && fightBecauseWayTooManyUnitsNear(unit)) {
//            return true;
//        }

//        if (enemies.onlyRanged() && ACombatEvaluator.isSituationFavorable(unit)) {
//        if (enemies.onlyMelee() && ACombatEvaluator.isSituationFavorable(unit)) {
//            return true;
//        }

        if (lurker != null && (!lurker.isBurrowed() || lurker.isDetected())) {
            unit.addLog("FightLurker");
            return true;
        }

//        if (tankSieged != null || tanks != null) {
//            return true;
//        }

        if (combatBuilding != null && unit.mission().allowsToAttackCombatBuildings(unit, combatBuilding)) {
            unit.addLog("FightBuilding");
            return true;
        }

        if (unit.isMelee()) {
            return fightAsMeleeUnit();
        } else {
            return fightAsRangedUnit();
        }
    }

    protected boolean dontFightInTopImportantCases() {

        // Always avoid invisible combat units
//        if (invisibleDT != null || invisibleCombatUnit != null) {
//            return true;
//        }

        return false;
    }

    protected boolean fightInImportantCases() {
        if (unit.isWorker()) {
            System.err.println("Worker in fightInImportantCases");
        }

        if (
            unit.isMelee()
                && unit.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon).inRadius(2.8, unit).notEmpty()
        ) {
            unit.addLog("DefendCannon");
            return true;
        }

        // Attacking critically important unit
        if (ATargetingCrucial.isCrucialUnit(unit.target())) {
            unit.setTooltipTactical("Crucial!");
            return true;
        }

        if (forbidMeleeUnitsAbandoningCloseTargets(unit)) {
            unit.setTooltipTactical("DontLeave");
            return true;
        }

        if (forbidAntiAirAbandoningCloseTargets(unit)) {
            return true;
        }

        return false;
    }

    // RANGED
    protected boolean fightAsRangedUnit() {
        if (melee != null && melee.hasPosition()) {
            unit.addLog("RunMelee" + A.dist(unit, melee));
//            unit.addLog("RunMelee");
            return false;
        }

        if (vulture != null) {
            unit.addLog("FightVulture");
            return true;
        }

        if (ranged != null) {
            if (unit.isTank() && !unit.isSieged() && unit.lastAttackFrameMoreThanAgo(30 * 4)) {
                unit.addLog("TankShoot");
                return true;
            }

            // Dragoon faster than Marines, can outrun them
            if (unit.isQuickerOrSameSpeedAs(enemies) && unit.hasBiggerRangeThan(enemies)) {
                if (unit.woundPercent() <= 40 && unit.lastUnderAttackMoreThanAgo(30 * 8)) {
                    unit.addLog("FightQuick");
                    return true;
                }
            }

            // Dragoon slower than Vultures, cannot outrun them
            else {
                unit.addLog("FightTooSlow");
                return true;
            }
        }

        if (ranged != null && !RetreatManager.shouldRetreat(unit)) {
            unit.addLog("CanFight");
            return true;
        }

        unit.addLog("DontFight");
        return false;
    }

    // MELEE
    protected boolean fightAsMeleeUnit() {
        if (invisibleDT != null || invisibleCombatUnit != null) {
            unit.addLog("RunInvisible");
            return false;
        }

        return !RetreatManager.shouldRetreat(unit);
//        return true;
    }

    // =========================================================

    private boolean finishOffAlmostDeadTarget(AUnit unit) {
        if (unit.cooldownRemaining() >= 5) {
            return false;
        }

        AUnit target = unit.target();
        if (target != null && target.type().totalCost() >= 70 && target.hp() <= (unit.damageAgainst(target) + 4)) {
            return true;
        }

        return false;
    }

    private boolean handleTerranInfantryShouldFight(AUnit unit) {
        if (!unit.isTerranInfantry()) {
            return false;
        }

        int meleeEnemiesNear = unit.enemiesNear().melee().inRadius(1.5, unit).count();
        if (unit.hp() <= (Enemy.protoss() ? 18 : 11) * meleeEnemiesNear) {
            return false;
        }

//        return false;
        boolean medicNear = unit.medicInHealRange();
        return medicNear || (!unit.isWounded() && ranged == null && unit.friendsInRadiusCount(1) >= 4);
    }

    protected boolean forbidMeleeUnitsAbandoningCloseTargets(AUnit unit) {
        return unit.isMelee()
//                && (!unit.isFirebat() || TerranFirebat.shouldContinueMeleeFighting(unit))
                && (
                    unit.isDT()
                    || (unit.hp() <= 30 && unit.enemiesNear().ranged().inRadius(6, unit).notEmpty())
                    || (unit.enemiesNear().ranged().inRadius(1, unit).isNotEmpty())
                    || (unit.enemiesNear().combatBuildings(false).inRadius(3, unit).isNotEmpty())
                );
    }

    protected boolean forbidAntiAirAbandoningCloseTargets(AUnit unit) {
        return unit.isAirUnitAntiAir()
                && unit.enemiesNear()
                .canBeAttackedBy(unit, 3)
                .isNotEmpty();
    }

    protected boolean fightBecauseWayTooManyUnitsNear(AUnit unit) {
        Selection our = unit.friendsNear().combatUnits();
        int allCount = unit.allUnitsNear().inRadius(0.3, unit).effVisible().count();
        int ourCount = our.inRadius(0.4, unit).count();

//        if (unit.mission() != null && unit.mission().isMissionAttack()) {
        boolean isStacked = false;
        if (We.terran()) {
            isStacked = allCount >= 6 || (invisibleDT != null && allCount >= 4)
                    || our.inRadius(1.3, unit).atLeast(25);
        }
        else if (We.protoss()) {
            isStacked = allCount >= 6 || (invisibleDT != null && allCount >= 4)
                    || our.inRadius(1.3, unit).atLeast(5);
        }
        else if (We.zerg()) {
            isStacked = allCount >= 6 || (invisibleDT != null && allCount >= 4)
                    || our.inRadius(1.3, unit).atLeast(5);
        }
//        }

        if (combatBuilding != null) {
            return unit.mission().isMissionAttack()
                    && unit.ourCombatUnitsNear(false).inRadius(6, unit).atLeast(10)
                    && ACombatEvaluator.advantagePercent(unit, 50);
//                    && A.printErrorAndReturnTrue("Fight DEF building cuz stacked " + unit.nameWithId());
        }

//        boolean isStacked = ourCount >= 5 || allCount >= 6;
//        boolean isStacked = ourCount >= 5 || allCount >= 6;

        if (isStacked) {
            unit.addLog("Stacked:" + ourCount + "/" + allCount);
        }

        return isStacked;
    }

    protected boolean fightAsWorker(AUnit unit, Units enemies) {
        if (combatBuilding != null || lurker != null || reaver != null || tankSieged != null || melee != null || invisibleCombatUnit != null) {
            return false;
        }

        return unit.hpPercent() > 75 && unit.distToLessThan(Select.main(), 12);
    }

}
