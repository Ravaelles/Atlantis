package atlantis.combat.micro.avoid;

import atlantis.combat.eval.HeuristicCombatEvaluator;
import atlantis.combat.retreating.ShouldRetreat;
import atlantis.combat.targeting.ATargetingCrucial;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class FightInsteadAvoid {

    private static Cache<Boolean> cache = new Cache<>();

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
        return cache.get(
            "shouldFight:" + unit.idWithHash(),
            5,
            () -> {
                if (unit.isMelee() && unit.shouldRetreat()) {
                    return false;
                }

                if (enemies.isEmpty()) {
                    System.err.println("NoEnemies? LooksBugged");
                    unit.addLog("NoEnemiesReally?");
                    return true;
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
        );
    }

    public static boolean shouldFightCached(AUnit unit) {
        String key = "shouldFight:" + unit.idWithHash();
        return cache.has(key) ? cache.get(key) : false;
    }

    // =========================================================

    protected boolean fightAsCombatUnit() {
        if (fightBecauseWayTooManyUnitsNear(unit)) {
            unit.addLog("FightStacked");
            return true;
        }

        if (ShouldRetreat.shouldRetreat(unit)) {
            if (finishOffAlmostDeadTarget(unit)) {
                unit.addLog("FatalityTo" + unit.target().type());
                return true;
            }

            if (
                unit.isRanged()
                    && ranged == null
                    && unit.enemiesNear().ranged().isEmpty()
                    && (unit.hp() >= 21 || unit.lastStartedAttackMoreThanAgo(30 * 7))
            ) {
                unit.addLog("FightAsRanged");
                return false;
            }
            else {
                unit.setTooltip("Retreat", true);
                unit.addLog("Retreat");
                return false;
            }
        }

//        if (handleTerranInfantryShouldFight(unit)) {
//            unit.addLog("InfantryFight");
//            return true;
//        }

//        if (combatBuilding != null && fightBecauseWayTooManyUnitsNear(unit)) {
//            return true;
//        }

//        if (enemies.onlyRanged() && ACombatEvaluator.isSituationFavorable(unit)) {
//        if (enemies.onlyMelee() && ACombatEvaluator.isSituationFavorable(unit)) {
//            return true;
//        }

        if (
            lurker != null && (!lurker.isBurrowed() || lurker.isDetected())
            && unit.noCooldown() && lurker.distToLessThan(unit, 4) && unit.friendsNearInRadius(3) >= 3
        ) {
            unit.addLog("FightLurker");
            return true;
        }

        if (tankSieged != null && unit.distToLessThan(tankSieged, 3)) {
            unit.addLog("FightSiegedTank");
            return true;
        }

        if (combatBuilding != null && unit.mission().allowsToAttackCombatBuildings(unit, combatBuilding)) {
            unit.addLog("FightBuilding");
            return true;
        }

        if (unit.isMelee()) {
            return fightAsMeleeUnit();
        }
        else {
            return false;
//            return fightAsRangedUnit();
        }
    }

    protected boolean fightInImportantCases() {
        if (unit.isWorker()) {
            System.err.println("Worker in fightInImportantCases");
        }

        if (forDragoon(unit)) {
            return true;
        }

        if (forTerranInfantry(unit)) {
            return true;
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
            unit.setTooltipTactical("DontAbandonCloseTargetz");
            return true;
        }

        return false;
    }

    private boolean forTerranInfantry(AUnit unit) {
        if (!unit.isMarine() && !unit.isGhost() && !unit.isFirebat()) {
            return false;
        }

        if (
            unit.hp() >= 38
            && unit.cooldownRemaining() <= 2
            && (unit.enemiesNearInRadius(2.4) <= 1 || unit.lastAttackFrameMoreThanAgo(30 * 3))
            && (
                unit.noCooldown()
//                || (unit.friendsNearInRadius(1.5) >= 5 || unit.enemiesNearInRadius(2) == 0)
                || unit.friendsNearInRadius(1.5) >= 5
            )
            && unit.friendsInRadius(3).medics().free().isNotEmpty()
            && (unit.isStimmed() || unit.friendsInRadius(3).atLeast(5))

//                unit.hp() >= 40
//                && unit.cooldownRemaining() <= 2
//                && (unit.enemiesNearInRadius(2) == 0 || unit.lastAttackFrameMoreThanAgo(30 * 3))
//                && (unit.isHealthy() || unit.enemiesNearInRadius(2.4) <= 1)
//                && unit.friendsInRadius(2.5).medics().free().isNotEmpty()
//                && (unit.isStimmed() || unit.friendsInRadius(3).atLeast(5))
        ) {
            unit.setTooltipTactical("SafeWithMedics");
            return true;
        }

        return false;
    }

    private boolean forDragoon(AUnit unit) {
        if (!unit.isDragoon()) {
            return false;
        }

        if (unit.cooldownRemaining() <= 3) {
            int secondsWithoutAttack = (int) (2 + unit.woundPercent() / 13);
            boolean haveNotAttackedInAWhile = unit.lastStartedAttackMoreThanAgo(30 * secondsWithoutAttack);
            if (unit.shieldDamageAtMost(10) || haveNotAttackedInAWhile) {
                if (unit.meleeEnemiesNearCount(2) <= 1 || haveNotAttackedInAWhile) {
                    unit.addLog("Aiur");
                    return true;
                }
            }
        }

//            oddal sie od contain focus pointu, bo na Destination stackują się na focusie
//            trzymaj ładny krąg na focusie

//        if (
//            // Long didn't shoot
//            unit.lastStartedAttackMoreThanAgo(30 * 5)
//                || (
//                // Relatively healthy
//                ((unit.hp() >= 33 && unit.cooldownRemaining() <= 5) || unit.shieldDamageAtMost(13))
//                    // Should fire by now
//                    && (unit.lastStartedAttackMoreThanAgo(30 * 2) || unit.lastUnderAttackMoreThanAgo(30 * 6))
//            )
//        ) {
//            unit.addLog("ForAiur");
//            return true;
//        }

        return false;
    }

    // RANGED
    protected boolean fightAsRangedUnit() {
        if (ranged != null && ranged.isBuilding()) {
            return false;
        }

        if (unit.isRanged() && melee != null && ranged == null) {
//            if (unit.hp() >= 40 && unit.lastAttackFrameMoreThanAgo(30 * 5)) {
            if (unit.hp() >= 40 && unit.lastAttackFrameMoreThanAgo(30 * 4) && unit.nearestEnemyDist() >= 2.9) {
                unit.addLog("Courage");
                return true;
            }
        }

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

//            // Dragoon faster than Marines, can outrun them
//            if (unit.isQuickerOrSameSpeedAs(enemies) && unit.hasBiggerRangeThan(enemies)) {
//                if (unit.woundPercent() <= 40 && unit.lastUnderAttackMoreThanAgo(30 * 8)) {
//                    unit.addLog("FightQuick");
//                    return true;
//                }
//            }
//
//            // Dragoon slower than Vultures, cannot outrun them
//            else {
//                unit.addLog("FightTooSlow");
//                return true;
//            }
        }

        if (ranged != null && !ShouldRetreat.shouldRetreat(unit)) {
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

        return !ShouldRetreat.shouldRetreat(unit);
//        return true;
    }

    // =========================================================

    private boolean finishOffAlmostDeadTarget(AUnit unit) {
        if (unit.cooldownRemaining() >= 5) {
            return false;
        }

        AUnit target = unit.target();
        if (target != null && target.type().totalCost() >= 70 && target.hp() <= (unit.damageAgainst(target) - 1)) {
            return true;
        }

        return false;
    }

//    private boolean handleTerranInfantryShouldFight(AUnit unit) {
//        if (!unit.isTerranInfantry()) {
//            return false;
//        }
//
//        int meleeEnemiesNear = unit.enemiesNear().melee().inRadius(1.5, unit).count();
//        if (unit.hp() <= (Enemy.protoss() ? 18 : 11) * meleeEnemiesNear) {
//            return false;
//        }
//
////        return false;
//        boolean medicNear = unit.medicInHealRange();
//        return medicNear || (!unit.isWounded() && ranged == null && unit.friendsInRadiusCount(1) >= 4);
//    }

    protected boolean forbidMeleeUnitsAbandoningCloseTargets(AUnit unit) {
        return unit.isMelee()
//                && (!unit.isFirebat() || TerranFirebat.shouldContinueMeleeFighting(unit))
            && (
            unit.isDT()
                || (unit.hp() <= 30 && unit.enemiesNear().ranged().inRadius(6, unit).notEmpty())
                || (unit.enemiesNear().ranged().inRadius(1, unit).isNotEmpty())
                || (unit.enemiesNear().combatBuildings(false).inRadius(3, unit).isNotEmpty())
                || (unit.enemiesNear().ofType(AUnitType.Protoss_Reaver).inRadius(3, unit).isNotEmpty())
        );
    }

    protected boolean forbidAntiAirAbandoningCloseTargets(AUnit unit) {
        return unit.isAirUnitAntiAir()
            && unit.enemiesNear()
            .canBeAttackedBy(unit, 3)
            .isNotEmpty();
    }

    protected boolean fightBecauseWayTooManyUnitsNear(AUnit unit) {
        if (!We.terran() || unit.isAir()) {
            return false;
        }

        Selection our = unit.friendsNear().combatUnits();
        int allCount = unit.allUnitsNear().inRadius(0.8, unit).effVisible().count();
        int ourCount = our.nonBuildings().inRadius(1, unit).count();

//        if (unit.mission() != null && unit.mission().isMissionAttack()) {
        boolean isStacked = false;
        if (We.terran()) {
            isStacked = allCount >= 7 || (invisibleDT != null && allCount >= 4)
                || our.inRadius(1.3, unit).atLeast(25);
        }
        else if (We.protoss()) {
            isStacked = allCount >= 7 || (invisibleDT != null && allCount >= 4)
                || our.inRadius(1.3, unit).atLeast(7);
        }
        else if (We.zerg()) {
            isStacked = allCount >= 6 || (invisibleDT != null && allCount >= 4)
                || our.inRadius(1.3, unit).atLeast(7);
        }
//        }

        if (combatBuilding != null) {
            return unit.mission().isMissionAttack()
                && unit.friendsNearInRadiusSelect(6).atLeast(10)
                && HeuristicCombatEvaluator.advantagePercent(unit, 50);
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
