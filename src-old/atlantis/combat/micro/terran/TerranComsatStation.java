package atlantis.combat.micro.terran;

import atlantis.architecture.Manager;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.TechType;

public class TerranComsatStation extends Manager {
    public TerranComsatStation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.is(AUnitType.Terran_Comsat_Station) && (AGame.everyNthGameFrame(13) || Env.isTesting());
    }

    @Override
    protected Manager handle() {
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
        if (!unit.energy(50)) return false;
        if (unit.energy(190)) return true;

        if (noMobileDetectionAndNotCloseToExistingBunker(lurker)) return false;

        Selection ourBuildingsClose = Select.ourBuildingsWithUnfinished().inRadius(6.5, lurker);
        if (
            ourBuildingsClose.isNotEmpty() && (
                ourBuildingsClose.bunkers().notEmpty()
                    || lurker.friendsNear().canAttack(lurker, 7).atLeast(2)
            )
        ) {
            return true;
        }

        int minUnitsNear = (unit.energy(160) ? 3 : (unit.energy(60) ? 4 : 6));

        return Select.ourCombatUnits()
            .excludeTypes(AUnitType.Terran_Medic)
            .inRadius(12, lurker)
            .atLeast(minUnitsNear);
    }

    private boolean noMobileDetectionAndNotCloseToExistingBunker(AUnit enemy) {
        return Count.scienceVessels() == 0
            && Count.bunkers() > 0
            && Select.ourOfType(AUnitType.Terran_Bunker).countInRadius(6.5, enemy) == 0;
    }

    // =========================================================
    // vs Protoss

    private boolean scanDarkTemplars() {
        for (AUnit dt : Select.enemy().effUndetected().ofType(AUnitType.Protoss_Dark_Templar).list()) {
            if (shouldScanThisDT(dt)) return scan(dt);
        }

        return false;
    }

    private boolean shouldScanThisDT(AUnit dt) {
        if (noMobileDetectionAndNotCloseToExistingBunker(dt)) return false;

        Selection ourCombatUnits = Select.ourCombatUnits();

        int minOurUnitsNear = unit.energy(150) ? (unit.energy(190) ? 2 : 4) : 7;
        if (A.seconds() <= 320) minOurUnitsNear = 2;

        if (ourCombatUnits.excludeTypes(AUnitType.Terran_Medic).inRadius(8, dt).atLeast(minOurUnitsNear)) {
            if (
                ourCombatUnits.nearestTo(dt).distToLessThan(dt, 6)
                    || ourCombatUnits.tanks().inRadius(12, dt).notEmpty()
            ) {
                return true;
            }
        }
        return false;
    }

    private boolean scanObservers() {
        if (unit.energy() <= 150) return false;

        for (AUnit observer : Select.enemy().effUndetected().ofType(AUnitType.Protoss_Observer).list()) {
            if (shouldScanThisObserver(observer)) {
                return scan(observer);
            }
        }

        return false;
    }

    private boolean shouldScanThisObserver(AUnit observer) {
        if (Select.ourRealUnits().inRadius(9, observer).havingAntiAirWeapon().atLeast(6)) return true;

        if (
            Select.enemies(AUnitType.Protoss_Carrier).inRadius(AUnit.NEAR_DIST, observer).isNotEmpty()
                && Select.ourRealUnits().inRadius(9, observer).atLeast(1)
        ) return true;

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
                .inRadius(7, enemy)
                .atLeast(3);
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

        unit.setTooltipTactical("Scanning " + unitToScan.name());
        return unit.useTech(TechType.Scanner_Sweep, unitToScan);
    }

}
