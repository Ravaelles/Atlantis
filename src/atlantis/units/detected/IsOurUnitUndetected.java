package atlantis.units.detected;

import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class IsOurUnitUndetected {
    private static Cache<Boolean> cache = new Cache<>();

    public static boolean check(AUnit unit) {
//        if (unit.isDarkTemplar()) System.out.println(unit.typeWithUnitId() + ": " + unit.isCloaked() + " " + unit.isBurrowed());

        if (!unit.isDarkTemplar() && !unit.isObserver()) {
            if (!unit.isCloaked() && !unit.isBurrowed()) return f("JustNotCloaked", unit);
        }
        if (unit.lastUnderAttackLessThanAgo(30 * 4)) return f("UnderAttack", unit);

        return cache.get(unit.idWithHash(), 7, () -> isUndetected(unit));
    }

    private static boolean isUndetected(AUnit unit) {
        double maxRange = 11.8;
        Selection detectorsNearby = detectors(unit).inRadius(maxRange, unit);
        if (detectorsNearby.notEmpty()) {
            return f("DetectorsNearby", unit);
        }

        if (We.terran()) {
            int lastCloakedAgo = Math.min(unit.lastActionAgo(Actions.CLOAK), unit.lastActionAgo(Actions.BURROW)) + 30;
            if (unit.lastUnderAttackLessThanAgo(lastCloakedAgo)) {
                return f("UnderAttackAfterCloak", unit);
            }
        }

        return true;
    }

    private static Selection detectors(AUnit unit) {
        Selection detectors = unit.enemiesNear().detectors();
        if (detectors.isEmpty()) return detectors;

        Units finalDetectors = new Units();

        if (Enemy.protoss()) {
            finalDetectors.addUnits(detectors.buildings().notUnpowered().inRadius(7, unit));
            finalDetectors.addUnits(detectors.observers().inRadius(9, unit));
        }

        else if (Enemy.zerg()) {
            finalDetectors.addUnits(detectors.buildings().sporeColonies().inRadius(7, unit));
            finalDetectors.addUnits(detectors.overlords().visibleOnMap().inRadius(9, unit));
        }

        else if (Enemy.terran()) {
            finalDetectors.addUnits(detectors.buildings().turrets().inRadius(7, unit));
            finalDetectors.addUnits(detectors.scienceVessels().visibleOnMap().inRadius(10, unit));
            finalDetectors.addUnits(detectors.ofType(AUnitType.Spell_Scanner_Sweep).visibleOnMap().inRadius(9, unit));
        }

        return Select.from(finalDetectors);
    }

    private static boolean f(String reason, AUnit unit) {
//        if (unit.isDarkTemplar()) System.out.println("Undetected fail: " + reason);
        return false;
    }
}
