package atlantis.combat.micro.avoid.fight;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.terran.fight.ShouldFightInsteadAvoidAsTerran;
import atlantis.combat.micro.avoid.terran.fight.TerranFightInsteadAvoid;
import atlantis.combat.micro.avoid.zerg.ShouldFightInsteadAvoidAsZerg;

import atlantis.combat.targeting.generic.ATargetingCrucial;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class ShouldAlwaysFightInsteadAvoid {

    private Cache<Boolean> cache = new Cache<>();

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
    private final TerranFightInsteadAvoid terranFightInsteadAvoid;

    // =========================================================

    public ShouldAlwaysFightInsteadAvoid(AUnit unit, Units enemies) {
        this.unit = unit;
        this.enemies = enemies;
        this.enemiesSelection = Select.from(enemies);

        terranFightInsteadAvoid = new TerranFightInsteadAvoid(unit);

        Selection selector = Select.from(enemies);
        invisibleDT = selector.ofType(AUnitType.Protoss_Dark_Templar).effUndetected().first();
        invisibleCombatUnit = selector.effUndetected().combatUnits().first();
        lurker = selector.ofType(AUnitType.Zerg_Lurker).first();
        tankSieged = selector.ofType(AUnitType.Terran_Siege_Tank_Siege_Mode).first();
        tanks = selector.tanks().first();
        vulture = selector.ofType(AUnitType.Terran_Vulture).first();
        reaver = selector.ofType(AUnitType.Protoss_Reaver).first();
        combatBuilding = selector.buildings().first();
        ranged = selector.ranged().first();
        melee = selector.melee().first();
    }

    // =========================================================

    public boolean shouldFight() {
        return cache.get(
            "shouldFight:" + unit.idWithHash(),
            3,
            () -> {
                if (!unit.hasAnyWeapon()) return false;

                Manager m;

                if ((m = new ShouldFightInsteadAvoidAsRanged(unit)).applies()) return true;
                if ((m = new ShouldFightInsteadAvoidAsTerran(unit)).invokeFrom(this) != null) return true;
                if (ShouldFightInsteadAvoidAsZerg.shouldFight(unit)) return true;

//                if (unit.isMelee() && unit.shouldRetreat()) return false;

                if (enemies.isEmpty()) {
//                    System.err.println("NoEnemies? LooksBugged");
//                    unit.addLog("NoEnemiesReally?");
//                    return true;
                    return false;
                }

                if (unit.mission() != null && unit.mission().forcesUnitToFight(unit, enemies)) {
                    unit.addLog("ForcedFight");
                    return true;
                }

                // Workers
                if (unit.isWorker()) {
                    return fightAsWorker(enemies);
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

    // =========================================================

    protected boolean fightAsCombatUnit() {
        if (fightBecauseWayTooManyUnitsNear()) {
            unit.addLog("FightStacked");
            return true;
        }

        if (finishOffAlmostDeadTarget()) {
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
            return true;
        }
//        else {
//            unit.setTooltip("Retreat", true);
//            unit.addLog("Retreat");
//            return false;
//        }

        if (terranFightInsteadAvoid.fightForTerran()) return true;

        // vs COMBAT BUILDINGS
        if (
            combatBuilding != null
                && unit.mission().allowsToAttackCombatBuildings(unit, combatBuilding)
                && unit.friendsInRadiusCount(2) >= 2
                && unit.friendsInRadiusCount(4) >= (unit.isAir() ? 14 : 6)
                && (!unit.isAir() || unit.woundPercentMax(15))
                && unit.eval() >= 3.2
                && (unit.hp() >= 23 || unit.isMelee())
        ) {
            unit.addLog("FightBuilding");
            return true;
        }

//        if (combatBuilding != null && fightBecauseWayTooManyUnitsNear()) {
//            return true;
//        }

//        if (enemies.onlyRanged() && ACombatEvaluator.isSituationFavorable()) {
//        if (enemies.onlyMelee() && ACombatEvaluator.isSituationFavorable()) {
//            return true;
//        }

        if (
            lurker != null && (!lurker.isBurrowed() || lurker.isDetected())
                && unit.noCooldown() && lurker.distToLessThan(unit, 4) && unit.friendsInRadiusCount(3) >= 3
        ) {
            unit.addLog("FightLurker");
            return true;
        }

        if (tankSieged != null && unit.distToLessThan(tankSieged, 3)) {
            unit.addLog("FightSiegedTank");
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

        if (forDragoon()) return true;

        if (terranFightInsteadAvoid.fightForTerran()) return true;

        if (
            unit.isMelee()
                && unit.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Sunken_Colony)
                .inRadius(2.8, unit).notEmpty()
        ) {
            unit.addLog("DefendCannon");
            return true;
        }

        // Attacking critically important unit
        if (ATargetingCrucial.isCrucialUnit(unit.target())) {
            unit.setTooltipTactical("Crucial!");
            return true;
        }

        if (forbidMeleeUnitsAbandoningCloseTargets()) {
            unit.setTooltipTactical("DontLeave");
            return true;
        }

        if (forbidAntiAirAbandoningCloseTargets()) {
            unit.setTooltipTactical("DontAbandonCloseTargetz");
            return true;
        }

        if (forWraith()) return true;

        return false;
    }

    private boolean forWraith() {
        if (!unit.isWraith()) return false;

        if (unit.hp() <= 40 || (unit.cooldown() <= 3 && !unit.isTank())) return false;

        if (
            unit.enemiesNear().effVisible().inRadius(12, unit).ofType(
                AUnitType.Protoss_Carrier,
                AUnitType.Zerg_Guardian
            ).notEmpty()
        ) {
            unit.setTooltip("AntiAirBravery");
            return true;
        }

        if (
            unit.enemiesNear().effVisible().inRadius(7, unit).ofType(
                AUnitType.Protoss_Arbiter,
                AUnitType.Terran_Battlecruiser,
                AUnitType.Terran_Wraith,
                AUnitType.Zerg_Mutalisk
            ).notEmpty()
        ) {
            unit.setTooltip("AntiAirBravery");
            return true;
        }

        return false;
    }

    private boolean forDragoon() {
        if (!unit.isDragoon()) return false;

        if (true) return false;

//        if (Enemy.protoss() && !unit.isSafeFromMelee()) return false;

        if (unit.cooldownRemaining() <= 3) {
            int secondsWithoutAttack = (int) (2 + unit.woundPercent() / 13.0);
            boolean haveNotAttackedInAWhile = unit.lastStartedAttackMoreThanAgo(30 * secondsWithoutAttack);
            if (unit.shieldDamageAtMost(8) || haveNotAttackedInAWhile) {
                if (unit.meleeEnemiesNearCount(2.8) <= 1 || haveNotAttackedInAWhile) {
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
        if (ranged != null && ranged.isABuilding()) return false;

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

        if (ranged != null) {
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

        return true;
    }

    // =========================================================

    private boolean finishOffAlmostDeadTarget() {
        if (unit.cooldownRemaining() >= 5) return false;

        AUnit target = unit.target();
        if (target != null && target.type().totalCost() >= 70 && target.hp() <= (unit.damageAgainst(target) - 1))
            return true;

        return false;
    }

//    private boolean handleTerranInfantryShouldFight() {
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

    protected boolean forbidMeleeUnitsAbandoningCloseTargets() {
        return unit.isMelee()
//                && (!unit.isFirebat() || TerranFirebat.shouldContinueMeleeFighting())
            && (
            unit.isDT()
                || (unit.hp() <= 30 && unit.enemiesNear().ranged().inRadius(6, unit).notEmpty())
                || (unit.enemiesNear().ranged().inRadius(1, unit).isNotEmpty())
                || (unit.enemiesNear().combatBuildings(false).inRadius(3, unit).isNotEmpty())
                || (unit.enemiesNear().ofType(AUnitType.Protoss_Reaver).inRadius(3, unit).isNotEmpty())
        );
    }

    protected boolean forbidAntiAirAbandoningCloseTargets() {
        return unit.isAirUnitAntiAir()
            && unit.enemiesNear()
            .canBeAttackedBy(unit, 3)
            .isNotEmpty();
    }

    protected boolean fightBecauseWayTooManyUnitsNear() {
        if (!We.terran() || unit.isAir()) return false;

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

//        if (combatBuilding != null) {
//            if (unit.mission().isMissionAttack() || unit.combatEvalRelative() >= 3.0) {
//                return true;
//            }
////                && unit.friendsInRadiusCountSelect(6).atLeast(10)
////                && HeuristicCombatEvaluator.advantagePercent(unit, 50);
////                    && A.printErrorAndReturnTrue("Fight DEF building cuz stacked " + unit.nameWithId());
//        }

//        boolean isStacked = ourCount >= 5 || allCount >= 6;
//        boolean isStacked = ourCount >= 5 || allCount >= 6;

        if (isStacked) {
            unit.addLog("Stacked:" + ourCount + "/" + allCount);
        }

        return isStacked;
    }

    protected boolean fightAsWorker(Units enemies) {
        if (enemies.size() >= 3) return false;

        if (combatBuilding != null || lurker != null || reaver != null || tankSieged != null || melee != null || invisibleCombatUnit != null)
            return false;

        return unit.hpPercent() > 75 && unit.distToLessThan(Select.main(), 12);
    }

}
