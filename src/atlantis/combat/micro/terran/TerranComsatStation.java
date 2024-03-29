package atlantis.combat.micro.terran;

import atlantis.architecture.Manager;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.TechType;
import tests.unit.FakeUnit;

public class TerranComsatStation extends Manager {
    public TerranComsatStation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.is(AUnitType.Terran_Comsat_Station) && AGame.everyNthGameFrame(13);
    }

    @Override
    public Manager handle() {
        if (unit.energy() >= 50) {
            if (
                scanLurkers()
                    || scanDarkTemplars()
                    || scanObservers()
                    || scanWraiths()
            ) return usedManager(this);
        }

        return null;
    }

    // =========================================================
    // vs Zerg

    private boolean scanLurkers() {
        for (AUnit lurker : Select.enemies(AUnitType.Zerg_Lurker).effUndetected().list()) {
            if (shouldScanThisLurker(lurker)) {
                return scan(lurker);
            }
        }

        return false;
    }

    private boolean shouldScanThisLurker(AUnit lurker) {
        if (unit.energy(190)) {
            return true;
        }

        int minUnitsNear = (unit.energy(160) ? 3 : (unit.energy(60) ? 4 : 6));

        if (unit.energy(100) && Select.ourBuildingsWithUnfinished().inRadius(6.5, lurker).isNotEmpty()) {
//            System.err.println("Scan " + lurker + " because buildings are close");
            return true;
        }

        return Select.ourCombatUnits()
            .excludeTypes(AUnitType.Terran_Medic)
            .inRadius(12, lurker)
            .atLeast(minUnitsNear);
    }

    // =========================================================
    // vs Protoss

    private boolean scanDarkTemplars() {
        for (AUnit dt : Select.enemy().effUndetected().ofType(AUnitType.Protoss_Dark_Templar).list()) {
            Selection ourCombatUnits = Select.ourCombatUnits();
            if (ourCombatUnits.excludeTypes(AUnitType.Terran_Medic).inRadius(8, dt)
                .atLeast(unit.energy(150) ? (unit.energy(190) ? 2 : 4) : 7)) {
                if (
                    ourCombatUnits.nearestTo(dt).distToLessThan(dt, 6)
                        || ourCombatUnits.tanks().inRadius(12, dt).notEmpty()
                ) {
                    return scan(dt);
                }
            }
        }

        return false;
    }

    private boolean scanObservers() {
        if (unit.energy() <= 200) {
            return false;
        }

        for (AUnit observer : Select.enemy().effUndetected().ofType(AUnitType.Protoss_Observer).list()) {
            if (shouldScanThisObserver(observer)) {
                return scan(observer);
            }
        }

        return false;
    }

    private boolean shouldScanThisObserver(AUnit observer) {
        if (unit.energy() >= 100 && Select.ourRealUnits().inRadius(9, observer).atLeast(1)) {
            return true;
        }

        if (
            Select.enemies(AUnitType.Protoss_Carrier).inRadius(15, observer).isNotEmpty()
                && Select.ourRealUnits().inRadius(9, observer).atLeast(1)
        ) {
            return true;
        }

        return false;
    }

    // =========================================================
    // vs Terran

    private boolean scanWraiths() {
        return genericScanUnit(AUnitType.Terran_Wraith);
    }

    private boolean scanGhosts() {
        return genericScanUnit(AUnitType.Terran_Ghost);
    }

    // =========================================================

    private boolean genericScanUnit(AUnitType type) {
        for (AUnit enemy : Select.enemies(type).effUndetected().list()) {
            boolean shouldScan = enemy.enemiesNear()
                .inRadius(6, enemy)
                .canAttack(enemy, 1)
                .atLeast(2);
            if (shouldScan) {
                return scan(enemy);
            }
        }

        return false;
    }

    private boolean scan(AUnit unitToScan) {
        if (!unitToScan.effUndetected()) {
            System.err.println("unitToScan is not effectively cloaked = " + unitToScan.hp());
            return false;
        }

//        if (unitToScan.isEffectivelyCloaked() && Select.enemy().exclude(unitToScan)
//                .cloakedButEffVisible().inRadius(3, unitToScan).isNotEmpty()) {
//            return false;
//        }

        if (!(unitToScan instanceof FakeUnit)) {
//            System.err.println("=== COMSAT SCAN on " + unitToScan + ", energy = " + comsat.energy() + " ===");
        }
        unit.setTooltipTactical("Scanning " + unitToScan.name());
        return unit.useTech(TechType.Scanner_Sweep, unitToScan);
    }

}
