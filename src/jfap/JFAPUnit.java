package jfap;

import atlantis.map.AMap;
import bwapi.*;

import java.util.Objects;

public class JFAPUnit implements Comparable<JFAPUnit> {
    public Unit unit;
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
    protected UnitType unitType = UnitType.Unknown;
    protected Player player = null;
    protected boolean isOrganic = false;
    protected int score = 0;
    protected int attackCooldownRemaining = 0;
    protected Race race = Race.Unknown;
    boolean didHealThisFrame = false;

    public JFAPUnit(Unit u) {
//        PlayerUnit pU = (PlayerUnit) u;
        Unit pU = u;
        unit = u;
        x = u.getX();
        y = u.getY();
        id = u.getID();
        UnitType auxType = u.getType();
        Player auxPlayer = pU.getPlayer();
        health = pU.getHitPoints();
        unitSize = auxType.size();
        maxHealth = auxType.maxHitPoints();
        armor = pU.getPlayer().armor(unit.getType());
        shields = pU.getShields();
        shieldArmor = auxPlayer.getUpgradeLevel(UpgradeType.Protoss_Plasma_Shields);
        maxShields = auxType.maxShields();
        speed = auxType.topSpeed();
        flying = auxType.isFlyer();
        groundDamage = auxType.groundWeapon().damageAmount();
        groundCooldown = auxType.groundWeapon().damageFactor() > 0 && auxType.maxGroundHits() > 0 ? auxType.groundWeapon().damageCooldown() /
                (auxType.groundWeapon().damageFactor() * auxType.maxGroundHits()) : 0;
        groundMaxRange = auxType.groundWeapon().maxRange();
        groundMinRange = auxType.groundWeapon().minRange();
        groundDamageType = auxType.groundWeapon().damageType();
        airDamage = auxType.airWeapon().damageAmount();
        airCooldown = auxType.airWeapon().damageFactor() > 0 && auxType.maxAirHits() > 0 ? auxType.airWeapon().damageCooldown() /
                auxType.airWeapon().damageFactor() * auxType.maxAirHits() : 0;
        airMaxRange = auxType.airWeapon().maxRange();
        airMinRange = auxType.airWeapon().minRange();
        airDamageType = auxType.airWeapon().damageType();
        unitType = auxType;
        player = auxPlayer;
        isOrganic = auxType.isOrganic();
        score = auxType.destroyScore();
        race = auxType.getRace();
        doThings(u, JFAP.game);
    }

    public JFAPUnit() {
    }

    private void doThings(Unit u, Game game) {
        if (unitType == UnitType.Protoss_Carrier) {
//            Carrier carrier = (Carrier) u;
            Unit carrier = u;
            groundDamage = UnitType.Protoss_Interceptor.groundWeapon().damageAmount();
            if (u != null && u.isVisible()) {
                final int interceptorCount = carrier.getInterceptorCount();
                if (interceptorCount > 0) groundCooldown = Math.round(37.0f / interceptorCount);
                else {
                    groundDamage = 0;
                    groundCooldown = 5;
                }
            } else if (player != null) {
                groundCooldown = Math.round(37.0f / (player.getUpgradeLevel(UpgradeType.Carrier_Capacity) == 1 ? 8 : 4));
            } else groundCooldown = Math.round(37.0f / 8);
            groundDamageType = UnitType.Protoss_Interceptor.groundWeapon().damageType();
            groundMaxRange = 32 * 8;
            airDamage = groundDamage;
            airDamageType = groundDamageType;
            airCooldown = groundCooldown;
            airMaxRange = groundMaxRange;
        } else if (unitType == UnitType.Terran_Bunker) {
            groundDamage = WeaponType.Gauss_Rifle.damageAmount();
            groundCooldown = UnitType.Terran_Marine.groundWeapon().damageCooldown() / 4;
            groundMaxRange = UnitType.Terran_Marine.groundWeapon().maxRange() + 32;
            airDamage = groundDamage;
            airCooldown = groundCooldown;
            airMaxRange = groundMaxRange;
        } else if (unitType == UnitType.Protoss_Reaver) groundDamage = WeaponType.Scarab.damageAmount();
        if (u != null) {
            if (UnitType.Terran_Marine.equals(u.getType())) {
                if (u.isStimmed()) {
                    groundCooldown /= 2;
                    airCooldown /= 2;
                }
            } else if (UnitType.Terran_Firebat.equals(u.getType())) {
                if (u.isStimmed()) {
                    groundCooldown /= 2;
                    airCooldown /= 2;
                }
            }
        }
        if (u != null && u.isVisible() && !u.isFlying()) {
//            elevation = game.getBWMap().getGroundHeight(u.getTilePosition());
//            u.getTilePosition().getGroundHeight()

            // @TODO
//            elevation = game.map().getGroundHeight(u.getTilePosition());
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
