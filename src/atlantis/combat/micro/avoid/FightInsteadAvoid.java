package atlantis.combat.micro.avoid;

import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.targeting.ATargetingCrucial;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.Units;

public class FightInsteadAvoid {

    private final AUnit unit;
    private final Units enemies;

    /**
     * Enemy units of different types that are dangerously close, extracted as variables for easier access
     */
    private final AUnit defensiveBuilding;
    private final AUnit invisibleDT;
    private final AUnit invisibleCombatUnit;
    private final AUnit lurkerOrReaver;
    private final AUnit tankSieged;
    private final AUnit tanks;
    private final AUnit reaver;
    private final AUnit vulture;
    private final AUnit ranged;
    private final AUnit melee;

    // =========================================================

    public FightInsteadAvoid(AUnit unit, Units enemies) {
        this.unit = unit;
        this.enemies = enemies;

        Select<AUnit> selector = Select.from(enemies);
        invisibleDT = selector.clone().ofType(AUnitType.Protoss_Dark_Templar).effCloaked().first();
        invisibleCombatUnit = selector.clone().effCloaked().combatUnits().first();
        lurkerOrReaver = selector.clone().ofType(AUnitType.Zerg_Lurker, AUnitType.Protoss_Reaver).first();
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

        // Attacking critically important unit
        if (reaver != null || ATargetingCrucial.isCrucialUnit(unit.getTarget())) {
            unit.setTooltip("CrucialTarget!");
            return true;
        }

        // Always avoid invisible combat units
        if (invisibleDT != null || invisibleCombatUnit != null) {
            return false;
        }

        // Workers
        if (unit.isWorker()) {
            return fightAsWorker(unit, enemies);
        }

        // Combat units
        else {
            return fightAsCombatUnit();
        }
    }

    // =========================================================

    // RANGED
    private boolean fightAsRangedUnit() {
        if (melee != null) {
            return false;
        }

        if (vulture != null) {
            return true;
        }

        if (ranged != null) {

            // Dragoon faster than Marines, can outrun them
            if (unit.isQuickerOrSameSpeedAs(enemies) && unit.hasBiggerRangeThan(enemies)) {

                // If needs to wait before next attack
                return unit.cooldownRemaining() <= 3 || unit.inActOfShooting();
            }

            // Dragoon slower than Vultures, cannot outrun them
            else {
//                return unit.hpPercent() > 50 && unit.getCooldownCurrent() <= 2 && unit.hasWeaponRange(ranged, 0);
                return true;
            }
        }

        return false;
    }

    // MELEE
    private boolean fightAsMeleeUnit() {
        if (invisibleDT != null || invisibleCombatUnit != null) {
            return false;
        }

        return RetreatManager.shouldNotRetreat(unit, enemies);
    }

    private boolean fightAsCombatUnit() {
        if (wayTooManyUnitsNearby(unit)) {
            return true;
        }

        if (tankSieged != null || tanks != null) {
            return true;
        }

        if (defensiveBuilding != null) {
            return unit.mission().allowsToAttackDefensiveBuildings();
        }

        if (unit.isMelee()) {
            return fightAsMeleeUnit();
        } else {
            return fightAsRangedUnit();
        }
    }

    // =========================================================

    private boolean wayTooManyUnitsNearby(AUnit unit) {
        int unitsNearby = Select.all().exclude(unit).inRadius(0.3, unit).count();
        int ourNearby = Select.our().exclude(unit).inRadius(0.3, unit).count();

        if (unit.mission().isMissionAttack()) {
            return ourNearby >= 3 || unitsNearby >= 5;
        }

        return ourNearby >= 5 || unitsNearby >= 6;
    }

    private boolean fightAsWorker(AUnit unit, Units enemies) {
        if (defensiveBuilding != null || lurkerOrReaver != null || tankSieged != null || melee != null || invisibleCombatUnit != null) {
            return false;
        }

        return unit.hpPercent() > 75 && unit.distToLessThan(Select.mainBase(), 12);
    }

}
