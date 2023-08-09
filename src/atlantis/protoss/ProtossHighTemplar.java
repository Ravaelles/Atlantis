
package atlantis.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.AGame;
import atlantis.information.tech.SpellCoordinator;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import bwapi.TechType;

import java.util.List;

public class ProtossHighTemplar extends Manager {

    public ProtossHighTemplar(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.is(AUnitType.Protoss_High_Templar);
    }

    @Override
    protected Manager handle() {
        if (dontDisturb()) return usedManager(this);
        if (tryMeldingIntoArchon()) return usedManager(this);

        if (AGame.everyNthGameFrame(3)) {
            if (handlePsionic()) return usedManager(this);
        }

        if (followArmy()) return usedManager(this);

        return null;
    }

    // =========================================================

    private boolean dontDisturb() {

        // Wants to Warp Archon
        if (
            unit.lastTechUsed() != null
                && unit.lastActionLessThanAgo(50, Actions.USING_TECH)
                && TechType.Archon_Warp.name().equals(unit.lastTechUsed().name())
                && unit.lastTechUnit().isAlive()
        ) {
            unit.setTooltipTactical("Sex & Archon");
            return true;
        }

        // Is target of Archon Warp
        for (AUnit otherHT : Select.ourOfType(AUnitType.Protoss_High_Templar).inRadius(3, unit).list()) {
            if (unit.equals(otherHT.target()) && unit.lastTechUsedAgo() <= 90) {
                unit.setTooltipTactical("Lets get it on");
                return true;
            }
        }

        if (unit.lastActionLessThanAgo(40, Actions.USING_TECH)) {
            unit.setTooltipTactical(unit.lastTechUsed().name() + "...");
            return true;
        }

        return false;
    }

    private boolean handlePsionic() {
        if (unit.energy() < 75) {
            unit.setTooltipTactical("NoEnergy");
            return false;
        }

        AUnit enemyToPsionic;
        if ((enemyToPsionic = veryCondensedEnemy(false)) != null) {
            return usePsionic(enemyToPsionic);
        }

        if ((enemyToPsionic = enemyCrucialUnit()) != null) {
            return usePsionic(enemyToPsionic);
        }

        if ((enemyToPsionic = enemyImportantUnit()) != null) {
            return usePsionic(enemyToPsionic);
        }

        return actWhenAlmostDead();
    }

    private boolean usePsionic(AUnit enemy) {
        if (SpellCoordinator.noOtherSpellAssignedHere(enemy.position(), TechType.Psionic_Storm)) {
            if (!enemy.isUnderStorm()) {
                unit.useTech(TechType.Psionic_Storm, enemy);
                return true;
            }
        }
        else {
//            System.err.println(
//                    "On " + A.now() + " " + unit.idWithHash() + "'s psionic was BLOCKED by another cast!"
//            );
        }

        return false;
    }

    private AUnit veryCondensedEnemy(boolean forceUsage) {
        Units condensedEnemies = new Units();
        for (AUnit enemy : Select.enemyRealUnits().inRadius(forceUsage ? 8.8 : 8.8, unit).list()) {
            if (!enemy.isUnderStorm()) {
                condensedEnemies.addUnitWithValue(enemy, Select.enemyRealUnits().inRadius(3.3, enemy).count());
            }
        }
        AUnit mostCondensedEnemy = condensedEnemies.unitWithHighestValue();

        if (mostCondensedEnemy != null) {
            int most = (int) condensedEnemies.valueFor(mostCondensedEnemy);
            unit.setTooltipTactical("Psionic?(" + most + " enemies)");

            int minUnitsInOnePlace = unit.energy() >= 180 ? 5 : 6;
            if (most >= minUnitsInOnePlace || forceUsage) {
                return mostCondensedEnemy;
            }
        }

        return null;
    }

    private AUnit enemyCrucialUnit() {
        List<? extends AUnit> enemyCrucialUnits = Select.enemy().ofType(
            AUnitType.Protoss_Reaver,
            AUnitType.Terran_Siege_Tank_Siege_Mode
        ).inRadius(12, unit).sortDataByDistanceTo(unit, false);

        for (AUnit enemy : enemyCrucialUnits) {
            if (Select.ourRealUnits().inRadius(2, enemy).atMost(2)) {
                return enemy;
            }
        }

        return null;
    }

    private AUnit enemyImportantUnit() {
        List<? extends AUnit> enemyCrucialUnits = Select.enemy().ofType(
            AUnitType.Protoss_Carrier,
            AUnitType.Protoss_Reaver,
            AUnitType.Terran_Science_Vessel,
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Terran_Siege_Tank_Siege_Mode,
            AUnitType.Zerg_Defiler
        ).inRadius(8.9, unit).sortDataByDistanceTo(unit, false);

        for (AUnit enemy : enemyCrucialUnits) {
            if (
                Select.ourRealUnits().inRadius(2, enemy).atMost(2)
                    && Select.enemyRealUnits().inRadius(3, enemy).atLeast(2)
            ) {
                return enemy;
            }
        }

        return null;
    }

    private boolean actWhenAlmostDead() {
        if (unit.hp() <= 31 && unit.energy() >= 75) {
            AUnit condensedEnemy;
            if ((condensedEnemy = veryCondensedEnemy(true)) != null) {
                return usePsionic(condensedEnemy);
            }
        }

        return false;
    }

    private boolean followArmy() {
        if (unit.hp() <= 16) {
            return false;
        }

        APosition center = Alpha.get().center();
        if (center != null) {
            if (Select.our().inRadius(0.3, unit).atLeast(3)) {
                return unit.moveAwayFrom(
                    Select.our().exclude(unit).nearestTo(unit),
                    1,
                    "Stacked",
                    Actions.MOVE_FORMATION
                );
            }

            if (
                center.distTo(unit) > 1
                    && unit.move(center, Actions.MOVE_FOLLOW, "Follow army", true)
            ) {
                return true;
            }
        }

        return false;
    }

    private boolean tryMeldingIntoArchon() {
        if (unit.energy() > 65 && unit.woundPercent() < 60) {
            return false;
        }

        Units lowEnergyHTs = new Units();
        for (AUnit other : Select.ourOfType(AUnitType.Protoss_High_Templar).inRadius(8, unit).list()) {
            if (other.energy() <= 70 || unit.woundPercent() >= 60) {
                lowEnergyHTs.addUnitWithValue(other, other.distTo(unit));
            }
        }

        AUnit closestOtherHT = lowEnergyHTs.unitWithLowestValue();
        if (closestOtherHT != null) {
//            if (closestOtherHT.distTo(unit) <= 0.9) {
            unit.useTech(TechType.Archon_Warp, closestOtherHT);
//                System.out.println("Warp Archon");
            unit.setTooltipTactical("WarpArchon");
            closestOtherHT.setTooltipTactical("OhArchon");
//                GameSpeed.changeSpeedTo(10);
//                CameraCommander.centerCameraOn();
//            }
//            else {
//                if (!unit.isMoving() && closestOtherHT.lastActionMoreThanAgo(90, UnitActions.USING_TECH)) {
//                    unit.useTech(TechType.Archon_Warp, closestOtherHT);
//                    unit.move(closestOtherHT, UnitActions.MOVE, "WarpArchon");
//                }
//                if (!closestOtherHT.isMoving() && closestOtherHT.lastActionMoreThanAgo(90, UnitActions.USING_TECH)) {
//                    closestOtherHT.useTech(TechType.Archon_Warp, unit);
//                    closestOtherHT.move(UnitActions.MOVE, "WarpArchon");
//                }
//            }
            return true;
        }

        return false;
    }


}
