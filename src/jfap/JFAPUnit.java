package jfap;

import atlantis.game.A;
import atlantis.game.APlayer;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.Vector;
import bwapi.*;
import jfap.tweaks.MoveCombatBuildingsCloserToOurUnits;

import java.util.Objects;

public class JFAPUnit implements Comparable<JFAPUnit> {
    public AUnit unit;
    protected int id = 0;
    protected int x = 0, y = 0;
    protected int health = 0;
    protected int maxHealth = 0;
    protected int armor = 0;
    protected int shields = 0;
    protected int shieldArmor = 0;
    protected int maxShields = 0;
    protected double speed = 0;
    protected boolean flying = false;
    protected int elevation = -1;
    protected UnitSizeType unitSize = UnitSizeType.Unknown;
    protected int groundDamage = 0;
    protected int groundCooldown = 0;
    protected int groundMaxRange = 0;
    protected int groundMinRange = 0;
    protected DamageType groundDamageType = DamageType.Unknown;
    protected int airDamage = 0;
    protected int airCooldown = 0;
    protected int airMaxRange = 0;
    protected int airMinRange = 0;
    protected DamageType airDamageType = DamageType.Unknown;
    protected AUnitType unitType = AUnitType.Unknown;
    protected APlayer player = null;
    protected boolean isOrganic = false;
    protected int score = 0;
    protected int attackCooldownRemaining = 0;
    protected Race race = Race.Unknown;
    boolean didHealThisFrame = false;

    public JFAPUnit(AUnit u) {
        AUnit pU = u;
        unit = u;
        x = u.x();
        y = u.y();

        if (u.isEnemy() && u.isCombatBuilding()) {
            Vector vector = (new MoveCombatBuildingsCloserToOurUnits(u)).vectorTowardsOurUnits();
            if (vector != null) {
//                vector.print("vector");
                x += (int) (vector.x * 32);
                y += (int) (vector.y * 32);

//                System.err.println("X now = " + A.digit(x / 32.0));
            }
        }

        id = u.id();
        AUnitType auxType = u.type();
        APlayer auxPlayer = pU.player();
        health = pU.hp();
        unitSize = auxType.ut().size();
        maxHealth = auxType.maxHp();
        armor = pU.player().armor(unit.type());
        shields = pU.shields();
        shieldArmor = auxPlayer.getUpgradeLevel(UpgradeType.Protoss_Plasma_Shields);
        maxShields = auxType.ut().maxShields();
        speed = auxType.ut().topSpeed();
        flying = auxType.ut().isFlyer();
        groundDamage = auxType.groundWeapon().damageAmount();
        groundCooldown = auxType.groundWeapon().damageFactor() > 0 && auxType.ut().maxGroundHits() > 0 ? auxType.groundWeapon().damageCooldown() /
            (auxType.groundWeapon().damageFactor() * auxType.ut().maxGroundHits()) : 0;
        groundMaxRange = auxType.groundWeapon().maxRange();
        groundMinRange = auxType.groundWeapon().minRange();
        groundDamageType = auxType.groundWeapon().damageType();
        airDamage = auxType.airWeapon().damageAmount();
        airCooldown = auxType.airWeapon().damageFactor() > 0 && auxType.ut().maxAirHits() > 0 ? auxType.airWeapon().damageCooldown() /
            auxType.airWeapon().damageFactor() * auxType.ut().maxAirHits() : 0;
        airMaxRange = auxType.airWeapon().maxRange();
        airMinRange = auxType.airWeapon().minRange();
        airDamageType = auxType.airWeapon().damageType();
        unitType = auxType;
        player = auxPlayer;
        isOrganic = auxType.isOrganic();
        score = auxType.ut().destroyScore();
        race = auxType.ut().getRace();
        doThings(u, JFAP.game);
    }

    public JFAPUnit() {
    }

    private void doThings(AUnit u, Game game) {
        if (u.isStasised() || u.isLockedDown()) {
            return;
        }

        if (unitType == AUnitType.Protoss_Carrier) {
            AUnit carrier = u;
            groundDamage = UnitType.Protoss_Interceptor.groundWeapon().damageAmount();
            if (u != null && u.isVisibleUnitOnMap()) {
                final int interceptorCount = carrier.u().getInterceptorCount();
                if (interceptorCount > 0) groundCooldown = Math.round(37.0f / interceptorCount);
                else {
                    groundDamage = 0;
                    groundCooldown = 5;
                }
            }
            else if (player != null) {
                groundCooldown = Math.round(37.0f / (player.getUpgradeLevel(UpgradeType.Carrier_Capacity) == 1 ? 8 : 4));
            }
            else groundCooldown = Math.round(37.0f / 8);
            groundDamageType = UnitType.Protoss_Interceptor.groundWeapon().damageType();
            groundMaxRange = 32 * 8;
            airDamage = groundDamage;
            airDamageType = groundDamageType;
            airCooldown = groundCooldown;
            airMaxRange = groundMaxRange;
        }
        else if (unitType == AUnitType.Terran_Bunker) {
            groundDamage = WeaponType.Gauss_Rifle.damageAmount();
            groundCooldown = UnitType.Terran_Marine.groundWeapon().damageCooldown() / 4;
            groundMaxRange = UnitType.Terran_Marine.groundWeapon().maxRange() + 32;
            airDamage = groundDamage;
            airCooldown = groundCooldown;
            airMaxRange = groundMaxRange;
        }
        else if (unitType == AUnitType.Protoss_Reaver) groundDamage = WeaponType.Scarab.damageAmount();
        if (u != null) {
            if (AUnitType.Terran_Marine.equals(u.type())) {
                if (u.isStimmed()) {
                    groundCooldown /= 2;
                    airCooldown /= 2;
                }
            }
            else if (AUnitType.Terran_Firebat.equals(u.type())) {
                if (u.isStimmed()) {
                    groundCooldown /= 2;
                    airCooldown /= 2;
                }
            }
        }
        if (u != null && u.isVisibleUnitOnMap() && !u.bwapiType().isFlyer()) {
//            elevation = game.getBWMap().getGroundHeight(u.getTilePosition());
//            u.getTilePosition().getGroundHeight()

            // @TODO
//            elevation = game.map().getGroundHeight(u.getTilePosition());
            elevation = 2;
        }
        groundMaxRange *= groundMaxRange;
        groundMinRange *= groundMinRange;
        airMaxRange *= airMaxRange;
        airMinRange *= airMinRange;
        health <<= 8;
        maxHealth <<= 8;
        shields <<= 8;
        maxShields <<= 8;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof JFAPUnit)) return false;
        JFAPUnit jfap = (JFAPUnit) o;
        return unit.equals(jfap.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(JFAPUnit arg0) {
        return this.id - arg0.id;
    }
}
