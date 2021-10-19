package atlantis.combat.micro.avoid;

import atlantis.combat.retreating.RetreatManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.util.A;

public class FightInsteadAvoid {

    private AUnit unit;
    private Units enemies;

    /**
     * Enemy units dangerously close, extracted as variables for easier access
     */
    private AUnit defensiveBuilding;
    private AUnit invisibleDT;
    private AUnit invisibleCombatUnit;
    private AUnit lurkerOrReaver;
    private AUnit tankSieged;
    private AUnit ranged;
    private AUnit melee;

    // =========================================================

    public FightInsteadAvoid(AUnit unit, Units enemies) {
        this.unit = unit;
        this.enemies = enemies;

        Select<AUnit> selector = Select.from(enemies);
        invisibleDT = selector.clone().ofType(AUnitType.Protoss_Dark_Templar).invisible().first();
        invisibleCombatUnit = selector.clone().invisible().combatUnits().first();
        lurkerOrReaver = selector.clone().ofType(AUnitType.Zerg_Lurker, AUnitType.Protoss_Reaver).first();
        tankSieged = selector.clone().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode).first();
        defensiveBuilding = selector.clone().buildings().first();
        ranged = selector.clone().ranged().first();
        melee = selector.clone().melee().first();
    }

    // =========================================================

    public boolean shouldFight() {
        if (enemies.isEmpty()) {
            return false;
        }

        if (invisibleDT != null || invisibleCombatUnit != null) {
            return false;
        }

        // Workers
        if (unit.isWorker()) {
            return fightAsWorker(unit, enemies);
        }

        // Combat units
        else {
            if (tankSieged != null) {
                return unit.mission().allowsToAttackEnemyUnit(unit, tankSieged);
            }

            if (defensiveBuilding != null) {
                return unit.mission().allowsToAttackDefensiveBuildings();
            }

            if (unit.isMeleeUnit()) {
                return fightAsMeleeUnit(unit, enemies);
            } else {
                return fightAsRangedUnit(unit, enemies);
            }
        }
    }

    // =========================================================

    private boolean fightAsRangedUnit(AUnit unit, Units enemies) {
        if (melee != null) {
            return false;
        }

        if (ranged != null) {

            // Dragoon faster than Marines, can outrun them
            if (unit.isQuickerThan(enemies)) {

                // If needs to wait before next attack
                if (unit.getCooldownCurrent() >= 5) {
                    return false;
                }

                return true;
            }

            // Dragoon slower than Vultures, cannot outrun them
            if (unit.isSlowerThan(enemies)) {
                if (unit.getCooldownCurrent() <= 2 && unit.hasWeaponRange(ranged, 0)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean fightAsMeleeUnit(AUnit unit, Units enemies) {
        if (invisibleDT != null) {
            return false;
        }

        return RetreatManager.shouldNotRetreat(unit, enemies);
    }

    // =========================================================

    private boolean fightAsWorker(AUnit unit, Units enemies) {
        if (defensiveBuilding != null || lurkerOrReaver != null || tankSieged != null || melee != null || invisibleCombatUnit != null) {
            return false;
        }

        return unit.HPPercent() > 64;
    }

}
