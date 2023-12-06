package atlantis.combat.micro.terran.vessel;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.TechType;

public class UseVesselTechs extends Manager {
    public UseVesselTechs(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.lastTechUsedAgo() <= 15) return true;

        if (unit.energy() <= 74) return false;
        if (!A.everyNthGameFrame(3)) return false;

        return true;
    }

    protected boolean print(String s) {
        System.err.println("Manager -> print:  " + s);
        return true;
    }

    @Override
    public Manager handle() {
        if (irradiate()) return usedManager(this);
        if (defensiveMatrix()) return usedManager(this);

        return null;
    }

    private boolean defensiveMatrix() {
        if (!unit.energy(100)) return false;
        if (!unit.energy(195) && unit.enemiesNear().empty()) return false;

        Selection combatUnits = unit.friendsNear().combatUnits();

        Selection targets = combatUnits.ofType(
            AUnitType.Terran_Science_Vessel,
            AUnitType.Terran_Siege_Tank_Siege_Mode,
            AUnitType.Terran_Siege_Tank_Tank_Mode
        );

        if (targets.isEmpty()) targets = combatUnits.wounded().havingWeapon();
        if (targets.isEmpty()) targets = combatUnits.havingWeapon();
        if (targets.isEmpty()) targets = combatUnits;

        targets = targets.sortByHealth();

        for (AUnit target : targets.list()) {
            if (target.isDefenseMatrixed()) continue;

            if (
                (target.isWounded() || target.lastUnderAttackLessThanAgo(120))
                    && target.enemiesNear().atLeast(1)
            ) {
//                System.out.println("@ " + A.now() + " - MATRIX A ON " + target);
                return unit.useTech(TechType.Defensive_Matrix, target);
            }
        }

        // Use on anyone as a fallback

        if (unit.energy(195)) {
            AUnit mostWounded = null;
            mostWounded = combatUnits.tanks().mostWounded();

            if (mostWounded != null) return unit.useTech(TechType.Defensive_Matrix, mostWounded);

            mostWounded = combatUnits.mostWounded();

            if (mostWounded != null) return unit.useTech(TechType.Defensive_Matrix, mostWounded);
        }

        return false;
    }

    private boolean irradiate() {
        if (!unit.energy(75) || !ATech.isResearched(TechType.Irradiate)) return false;

        Selection enemies = Select.enemyCombatUnits().inRadius(10, unit);
        if (enemies.count() >= 5 || (enemies.count() >= 3 && unit.energy(181))) {
            APosition center = enemies.center();
            if (center != null) {
                center = Select.enemyCombatUnits().inRadius(3, center).center();
            }
            else {
                center = enemies.random().position();
            }

            if (center != null) {
                return usingIrradiate(center);
            }
            else {
                System.err.println("Irradiate center is NULL / " + enemies.count());
                return usingIrradiate(enemies.first().position());
            }
        }

        // Crucial enemies
        AUnit enemy = Select.enemy().ofType(
            AUnitType.Zerg_Lurker, AUnitType.Zerg_Mutalisk, AUnitType.Zerg_Ultralisk, AUnitType.Zerg_Defiler,
            AUnitType.Zerg_Guardian, AUnitType.Zerg_Scourge,
            AUnitType.Protoss_High_Templar, AUnitType.Protoss_Archon, AUnitType.Protoss_Dark_Archon,
            AUnitType.Terran_Medic
        ).effVisible().inRadius(15, unit).mostDistantTo(unit);
        if (enemy != null) {
            return unit.useTech(TechType.Irradiate, enemy);
        }

        // Regular enemies
        enemy = Select.enemy().ofType(
            AUnitType.Zerg_Zergling, AUnitType.Zerg_Drone,
            AUnitType.Protoss_Dragoon, AUnitType.Protoss_Zealot,
            AUnitType.Terran_Marine
        ).effVisible().inRadius(15, unit).mostDistantTo(unit);
        if (enemy != null) {
            return unit.useTech(TechType.Irradiate, enemy);
        }

        return false;
    }

    private boolean usingIrradiate(APosition center) {
        unit.setTooltipTactical("Irradiate!");
        System.err.println("Irradiate center is " + center);
        return unit.useTech(TechType.Irradiate, center);
    }

    private boolean empShockwave() {
        Selection enemies = Select.enemyCombatUnits().inRadius(10, unit);

        if (enemies.count() >= 7 || (enemies.count() >= 4 && unit.energy(180))) {
            APosition center = enemies.center();
            if (center != null) {
                center = Select.enemyCombatUnits().inRadius(3, center).center();
            }
            else {
                center = enemies.random().position();
            }

            return unit.useTech(TechType.EMP_Shockwave, center);
        }

        return false;
    }
}
