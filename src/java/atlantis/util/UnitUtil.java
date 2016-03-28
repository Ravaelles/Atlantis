package atlantis.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import atlantis.debug.tooltip.TooltipManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

import bwapi.UnitType;
import bwapi.WeaponType;
import bwapi.Position;

public class UnitUtil {

    /**
     * Returns the total cost of a unit for score calculation, considering the Zergling special case (half
     * cost per unit)
     *
     * @param u
     * @return
     */
    public static int getTotalPrice(AUnitType type) {
        int total = type.gasPrice() + type.mineralPrice();
        if (type.equals(AUnitType.Zerg_Zergling)) {
            total /= 2;
        }
        return total;
    }

    /**
     * Returns median PX and median PY for all units.
     */
    public static Position medianPosition(Collection<AUnit> units) {
        if (units.isEmpty()) {
            return null;
        }

        ArrayList<Integer> xCoordinates = new ArrayList<>();
        ArrayList<Integer> yCoordinates = new ArrayList<>();
        for (AUnit unit : units) {
            xCoordinates.add(unit.getPosition().getX());	//TODO: check whether position is in Pixels
            yCoordinates.add(unit.getPosition().getX());
        }
        Collections.sort(xCoordinates);
        Collections.sort(yCoordinates);

        return new Position(
                xCoordinates.get(xCoordinates.size() / 2),
                yCoordinates.get(yCoordinates.size() / 2)
        );
    }

    public static boolean isBase(AUnitType t) {
        return isType(t, AUnitType.Terran_Command_Center, AUnitType.Protoss_Nexus, AUnitType.Zerg_Hatchery,
                AUnitType.Zerg_Lair, AUnitType.Zerg_Hive);
    }

    /**
     * Returns which unit of the same type this unit is. E.g. it can be first (0) Overlord or third (2)
     * Zergling. It compares IDs of units to return correct result.
     */
    public static int getUnitIndex(AUnit u) {
        int index = 0;
        Collection<AUnit> ourUnitsOfType = (Collection<AUnit>) Select.our().ofType(u.getType()).listUnits();
        for (AUnit otherUnit : ourUnitsOfType) {
            if (otherUnit.getID() < u.getID()) {
                index++;
            }
        }
        return index;
    }

    /**
     * Returns true if given type equals to one of types passed as parameter.
     */
    public static boolean isType(AUnitType t, AUnitType... types) {
        for (AUnitType otherType : types) {
            if (t.equals(otherType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether AUnitType is Refinery, Assimilator or Extractor
     *
     * @param t
     * @return
     */
    public static boolean isGasBuilding(AUnitType t) {
        return isType(t, AUnitType.Terran_Refinery, AUnitType.Protoss_Assimilator, AUnitType.Zerg_Extractor);
    }

    /**
     * Not that we're racists, but spider mines and larvas aren't really units...
     */
    public static boolean isNotActuallyUnit(AUnitType t) {
        return isType(t, AUnitType.Terran_Vulture_Spider_Mine, AUnitType.Zerg_Larva, AUnitType.Zerg_Egg);
    }

    /**
     * Replaces variable _isMilitaryBuildingAntiGround of old AUnit class
     *
     * @param t
     * @return
     */
    public static boolean isMilitaryBuildingAntiGround(AUnitType t) {
        return isType(
                t, AUnitType.Terran_Bunker, AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Sunken_Colony
        );
    }

    /**
     * Replaces variable _isMilitaryBuildingAntiAir of old AUnit class
     *
     * @param t
     * @return
     */
    public static boolean isMilitaryBuildingAntiAir(AUnitType t) {
        return isType(
                t, AUnitType.Terran_Bunker, AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Spore_Colony
        );
    }

    /**
     * Returns true if given unit type is one of buildings like Bunker, Photon Cannon etc. For more details,
     * you have to specify at least one <b>true</b> to the params.
     */
    public static boolean isMilitaryBuilding(AUnitType t, boolean canShootGround, boolean canShootAir) {
        if (!t.isBuilding()) {
            return false;
        }
        if (canShootGround && isMilitaryBuildingAntiGround(t)) {
            return true;
        } else if (canShootAir && isMilitaryBuildingAntiAir(t)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the remaining AUnit life, in %
     *
     * @param u
     * @return
     */
    public static int getHPPercent(AUnit u) {
        return 100 * u.getHitPoints() / u.getMaxHitPoints();
    }

    /**
     * Returns the 'normalized' damage of a UnitType 'normalized' is damageAmount * damageFactor
     *
     * @param t
     * @return
     */
    public static int getNormalizedDamage(WeaponType wt) {
        return wt.damageAmount() * wt.damageFactor();
    }

    /**
     * Returns true if unit has ground weapon. Replaces Unit.canAttackGroundUnits
     */
    public static boolean attacksGround(AUnit u) {
        return u.getGroundWeapon() != WeaponType.None;
    }

    /**
     * Returns true if unit has anti-air weapon. Replaces Unit.canAttackAirUnits
     */
    public static boolean attacksAir(AUnit unit) {
        return unit.getAirWeapon() != WeaponType.None;
    }

    /**
     * Returns true if attacker is capable of attacking <b>victim</b>. For example Zerglings can't attack
     * flying targets and Corsairs can't attack ground targets.
     *
     * @param includeCooldown if true, then unit will be considered able to attack only if the cooldown after
     * the last shot allows it
     */
    public static boolean canAttack(AUnit attacker, AUnit victim, boolean includeCooldown) {

        // Enemy is GROUND unit
        if (!victim.isAirUnit()) {
            return attacksGround(attacker) && (!includeCooldown || attacker.u().getGroundWeaponCooldown() == 0);
        } // Enemy is AIR unit
        else {
            return attacksAir(attacker) && (!includeCooldown || attacker.u().getAirWeaponCooldown() == 0);
        }
    }

}
