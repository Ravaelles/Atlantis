package tests.acceptance;

import atlantis.combat.targeting.generic.ATargeting;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.attacked_by.UnderAttack;
import atlantis.util.Angle;
import atlantis.map.position.Vectors;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import java.util.function.Consumer;
import java.util.function.IntPredicate;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Behaviour of {@link atlantis.units.AUnit} itself: type predicates, health and
 * energy arithmetic, weapon range, facing geometry and the "how long ago"
 * timestamps.
 *
 * <h2>Harness rules used here (see DOCS/TESTING.md)</h2>
 * <ul>
 *   <li><b>No world, no mocks</b> for anything that only reads a unit's own
 *       type or fields - the cheapest and least order-sensitive option.</li>
 *   <li><b>{@code world(1, ours, enemies, eachFrame)}</b> only where a
 *       query reads the unit collections ({@code friendsNear()},
 *       {@code enemiesNear()}, {@code allUnitsNear()}, nearest-enemy).</li>
 *   <li><b>{@code world(...)}</b> only where the unit
 *       under test needs its enemies visible without a frame loop.</li>
 * </ul>
 *
 * <h2>Two semantics that read wrong but are pinned on purpose</h2>
 * <ul>
 *   <li>{@code isOtherFacingThisUnit(other)} asks whether <b>other</b>
 *       faces <i>this</i> unit (direction other → this, tolerance 1.1 rad).</li>
 *   <li>{@code isOtherShowingBackToUs(other)} asks the opposite question with
 *       the other reference direction (this → other, tolerance 0.95 rad). Both
 *       are asserted against the raw vectors in
 *       {@code facingHelperAgreesWithTheRawVector} rather than against angles
 *       somebody guessed.</li>
 *   <li>{@code shieldPercent()} is undefined - NaN - for a unit that has no
 *       shields at all (Terran, Zerg, Protoss before the battery). That is the
 *       contract, not an oversight: it makes both
 *       {@code shieldWound() <= x} and {@code shieldWound() >= x} read as "not
 *       my rule". Pinned in {@code shieldsOnAUnitThatHasNone} so a replacement
 *       value shows up as a deliberate change, not a silent one.</li>
 * </ul>
 *
 * <p>The previous version of this class ran against the 22-unit sample world
 * that {@code world(1, eachFrame)} silently builds, asserted on instance
 * fields that only existed as a side effect of the world generators (so
 * {@code ourAndEnemyCount} threw NPE when run alone), and contained assertions
 * that could not fail. None of that is left here.</p>
 */
public class AUnitTest extends AbstractTestWithWorld {

    /**
     * Only {@code world(1, eachFrame)} asks the harness for
     * these. Every test below that needs units passes them explicitly, because
     * that overload silently builds the 22-unit sample world from UnitTest
     * instead - which is how the previous version of this class ended up
     * asserting against units it never declared.
     */
    @Override
    protected FakeUnit[] generateOur() {
        return fakeOurs(fake(AUnitType.Terran_Marine, 10));
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return fakeEnemies(fake(AUnitType.Zerg_Zergling, 15));
    }

    // =========================================================
    // Type predicates: no world, no mocks

    @Test
    public void meleeOrRanged() {
        assertTrue(fake(AUnitType.Terran_Marine).isRanged());
        assertFalse(fake(AUnitType.Terran_Marine).isMelee());

        assertTrue(fake(AUnitType.Terran_Firebat).isMelee());
        assertTrue(fake(AUnitType.Terran_SCV).isMelee());

        assertTrue(fake(AUnitType.Terran_Vulture).isRanged());
        assertTrue(fake(AUnitType.Terran_Wraith).isRanged());
        assertTrue(fake(AUnitType.Terran_Siege_Tank_Siege_Mode).isRanged());

        assertTrue(fake(AUnitType.Protoss_Zealot).isMelee());
        assertTrue(fake(AUnitType.Protoss_Dark_Templar).isMelee());
    }

    @Test
    public void typeChecksExtended() {
        assertTrue(fake(AUnitType.Terran_Goliath).isGoliath());
        assertTrue(fake(AUnitType.Zerg_Hydralisk).isHydralisk());
        assertTrue(fake(AUnitType.Terran_Command_Center).isCommandCenter());
        assertTrue(fake(AUnitType.Protoss_Corsair).isCorsair());
        assertTrue(fake(AUnitType.Protoss_Reaver).isReaver());
        assertTrue(fake(AUnitType.Protoss_Shuttle).isShuttle());
        assertTrue(fake(AUnitType.Protoss_High_Templar).isHighTemplar());
        assertTrue(fake(AUnitType.Protoss_Carrier).isCarrier());
        assertTrue(fake(AUnitType.Zerg_Scourge).isScourge());
        assertTrue(fake(AUnitType.Zerg_Defiler).isDefiler());
        assertTrue(fake(AUnitType.Zerg_Ultralisk).isUltralisk());

        assertTrue(fake(AUnitType.Zerg_Lurker).isLurker());
        assertFalse(fake(AUnitType.Zerg_Lurker).isUltralisk(), "a Lurker Den is not an Ultralisk");

        FakeUnit darkTemplar = fake(AUnitType.Protoss_Dark_Templar);
        assertTrue(darkTemplar.isDT());
        assertTrue(darkTemplar.isDarkTemplar());
    }

    @Test
    public void raceChecks() {
        assertTrue(fake(AUnitType.Protoss_Zealot).isProtoss());
        assertFalse(fake(AUnitType.Protoss_Zealot).isTerran());
        assertFalse(fake(AUnitType.Protoss_Zealot).isZerg());

        assertTrue(fake(AUnitType.Terran_Marine).isTerran());
        assertFalse(fake(AUnitType.Terran_Marine).isProtoss());

        assertTrue(fake(AUnitType.Zerg_Zergling).isZerg());
        assertFalse(fake(AUnitType.Zerg_Zergling).isTerran());
    }

    @Test
    public void realUnitsAreTheOnesTheEngineWouldReport() {
        // Non-real units are engine illusions; the bot must not treat them as
        // targets or count them as army.
        assertTrue(fake(AUnitType.Terran_Marine).isRealUnit());
        assertTrue(fake(AUnitType.Terran_Vulture_Spider_Mine).isRealUnit());
        assertTrue(fake(AUnitType.Protoss_Zealot).isRealUnit());
        assertTrue(fake(Protoss_Photon_Cannon).isRealUnit());
        assertTrue(fake(Zerg_Creep_Colony).isRealUnit());

        assertFalse(fake(AUnitType.Protoss_Scarab).isRealUnit());
        assertFalse(fake(AUnitType.Zerg_Egg).isRealUnit());
        assertFalse(fake(AUnitType.Zerg_Lurker_Egg).isRealUnit());
    }

    @Test
    public void combatBuildings() {
        for (AUnitType type : new AUnitType[]{
            Zerg_Sunken_Colony, Zerg_Spore_Colony, Zerg_Creep_Colony,
            Terran_Missile_Turret, Terran_Bunker, Protoss_Photon_Cannon,
        }) {
            FakeUnit building = fake(type);
            assertTrue(building.isCombatBuilding(), type.name() + " should be a combat building");
            assertTrue(building.isCombatUnit(), type.name() + " should count as a combat unit");
            assertTrue(building.isRealUnit(), type.name() + " should be a real unit");
        }

        assertFalse(fake(Zerg_Lurker).isCombatBuilding(),
            "a Lurker Den is a defensive structure, not a combat building");
    }

    @Test
    public void typeCharacteristics() {
        FakeUnit scv = fake(AUnitType.Terran_SCV);
        assertTrue(scv.isWorker());
        assertFalse(scv.isABuilding());
        assertTrue(scv.isMechanical());

        FakeUnit barracks = fake(AUnitType.Terran_Barracks);
        assertFalse(barracks.isWorker());
        assertTrue(barracks.isABuilding());
        assertTrue(barracks.isMechanical());

        assertTrue(fake(AUnitType.Terran_Marine).isMarine());
        assertFalse(fake(AUnitType.Terran_Marine).isMechanical());

        assertTrue(fake(AUnitType.Terran_Vulture).isVulture());
        assertTrue(fake(AUnitType.Terran_Vulture).isMechanical());

        assertTrue(fake(AUnitType.Protoss_Dragoon).isDragoon());
        assertTrue(fake(AUnitType.Protoss_Dragoon).isMechanical());
        assertTrue(fake(AUnitType.Terran_Siege_Tank_Tank_Mode).isTank());
    }

    @Test
    public void unitProperties() {
        FakeUnit cc = fake(AUnitType.Terran_Command_Center);
        assertTrue(cc.isBase());
        assertFalse(cc.isInfantry());
        assertFalse(cc.isFlying());
        assertTrue(cc.canBeRepaired());
        assertFalse(cc.canBeHealed());

        FakeUnit marine = fake(AUnitType.Terran_Marine);
        assertFalse(marine.isBase());
        assertTrue(marine.isInfantry());
        assertFalse(marine.isFlying());
        assertFalse(marine.canBeRepaired());
        assertTrue(marine.canBeHealed());

        FakeUnit medic = fake(AUnitType.Terran_Medic);
        assertTrue(medic.isMedic());
        assertTrue(medic.canBeHealed());

        FakeUnit wraith = fake(AUnitType.Terran_Wraith);
        assertTrue(wraith.isFlying());
        assertTrue(wraith.canBeRepaired());

        FakeUnit lifted = fake(AUnitType.Terran_Barracks);
        assertFalse(lifted.isFlying());
        lifted.lifted = true;
        assertTrue(lifted.isFlying(), "a lifted building is a flying unit");
    }

    @Test
    public void unitClassification() {
        assertTrue(fake(AUnitType.Protoss_Dark_Templar).canBeLonelyUnit());
        assertTrue(fake(AUnitType.Terran_Vulture).canBeLonelyUnit());
        assertFalse(fake(AUnitType.Terran_Marine).canBeLonelyUnit(),
            "a marine must stay with the army");

        assertTrue(fake(AUnitType.Terran_Siege_Tank_Tank_Mode).isCrucialUnit());
        assertTrue(fake(AUnitType.Protoss_High_Templar).isCrucialUnit());
        assertFalse(fake(AUnitType.Terran_Marine).isCrucialUnit());
    }

    @Test
    public void capabilityChecks() {
        assertTrue(fake(AUnitType.Terran_Wraith).canCloak());
        assertFalse(fake(AUnitType.Terran_Marine).canCloak());

        assertTrue(fake(Protoss_Corsair).isAirUnitAntiAir(), "Corsair shoots air");
        assertFalse(fake(AUnitType.Terran_Wraith).isAirUnitAntiAir(), "Wraith shoots ground");

        assertTrue(fake(AUnitType.Terran_Marine).isCombatUnit());
        assertFalse(fake(AUnitType.Terran_SCV).isCombatUnit());

        assertTrue(fake(AUnitType.Terran_Wraith).isRepairable(), "mech");
        assertFalse(fake(AUnitType.Terran_Marine).isRepairable(), "bio");
        assertFalse(fake(Protoss_Corsair).isRepairable());

        assertFalse(fake(AUnitType.Terran_Marine).isMissionDefendOrSparta());
    }

    @Test
    public void comparisonLogic() {
        FakeUnit vulture = fake(AUnitType.Terran_Vulture);
        FakeUnit marine = fake(AUnitType.Terran_Marine);
        FakeUnit tank = fake(AUnitType.Terran_Siege_Tank_Siege_Mode);

        assertTrue(vulture.isTypeQuickerOrSameSpeedAs(marine));
        assertFalse(marine.isTypeQuickerOrSameSpeedAs(vulture));

        assertTrue(tank.hasBiggerWeaponRangeThan(marine));
        assertFalse(marine.hasBiggerWeaponRangeThan(tank));
    }

    // =========================================================
    // Weapon ranges

    @Test
    public void combatCapabilities() {
        FakeUnit marine = fake(AUnitType.Terran_Marine);
        assertTrue(marine.canAttackGroundUnits());
        assertTrue(marine.canAttackAirUnits());
        assertTrue(marine.hasGroundWeapon());
        assertTrue(marine.hasAirWeapon());
        assertEquals(4, marine.groundWeaponRange());
        assertEquals(4.0, marine.airWeaponRange(), 0.001);

        FakeUnit zealot = fake(AUnitType.Protoss_Zealot);
        assertTrue(zealot.canAttackGroundUnits());
        assertFalse(zealot.canAttackAirUnits());
        assertFalse(zealot.isRanged());
        assertTrue(zealot.hasGroundWeapon());
        assertTrue(zealot.hasAnyWeapon());
        assertEquals(0, zealot.groundWeaponRange(), "melee range is 0");
        assertEquals(-1.0, zealot.airWeaponRange(), 0.001, "no weapon at all is reported as -1");

        FakeUnit wraith = fake(AUnitType.Terran_Wraith);
        assertTrue(wraith.canAttackGroundUnits());
        assertTrue(wraith.canAttackAirUnits());
        assertTrue(wraith.hasGroundWeapon());
        assertTrue(wraith.hasAirWeapon());

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon);
        assertTrue(dragoon.canAttackGroundUnits());
        assertTrue(dragoon.canAttackAirUnits());
        assertTrue(dragoon.hasAnyWeapon());
        // 4 tiles in the stub world: the Phase Disruptor's base reach is
        // 128 px, and nothing here researches Singularity Charge (which takes
        // it to 6). Upgrade-aware callers read OurDragoonRange instead; this
        // getter answers the weapon's own numbers.
        assertEquals(4.0, dragoon.airWeaponRange(), 0.001);

        FakeUnit observer = fake(AUnitType.Protoss_Observer);
        assertFalse(observer.canAttackGroundUnits());
        assertFalse(observer.canAttackAirUnits());
        assertFalse(observer.hasAnyWeapon());
        assertFalse(observer.hasGroundWeapon());
        assertFalse(observer.hasAirWeapon());
        assertEquals(-1, observer.groundWeaponRange());
        assertEquals(-1.0, observer.airWeaponRange(), 0.001);
        assertTrue(observer.hasNoWeaponAtAll());
    }

    /**
     * A sieged tank may not shoot point blank - that is a game rule, not a
     * range table entry, and it is the reason {@code hasWeaponRangeToAttack}
     * is not a pure distance comparison.
     */
    @Test
    public void siegedTankCannotShootPointBlank() {
        FakeUnit tank = fake(AUnitType.Terran_Siege_Tank_Siege_Mode, 10);
        FakeUnit adjacent = fake(AUnitType.Zerg_Zergling, 11);
        FakeUnit atRange = fake(AUnitType.Zerg_Zergling, 10 + tank.weaponRangeAgainst(adjacent));

        assertTrue(tank.isTankSieged());
        assertFalse(tank.hasWeaponRangeToAttack(adjacent, 0),
            "distance 1 is inside max range but a sieged tank cannot fire there");
        assertTrue(tank.hasWeaponRangeToAttack(atRange, 0),
            "exactly at max range is in range (the boundary is inclusive)");
        assertFalse(tank.hasWeaponRangeToAttack(atRange, -1),
            "a negative margin shrinks the range");
    }

    @Test
    public void weaponRangeFollowsTheTargetType() {
        FakeUnit wraith = fake(AUnitType.Terran_Wraith, 10);
        FakeUnit den = fake(AUnitType.Zerg_Hydralisk_Den, 11);
        FakeUnit farAway = fake(AUnitType.Zerg_Zergling, 10 + wraith.weaponRangeAgainst(den) + 1);

        assertTrue(wraith.hasWeaponRangeToAttack(den, 0));
        assertTrue(wraith.canAttackTarget(den, true, true, true, 0));
        assertFalse(wraith.hasWeaponRangeToAttack(farAway, 0));
        assertFalse(wraith.canAttackTarget(farAway, true, true, true, 0));

        // The margin widens the range by exactly that many tiles.
        assertTrue(wraith.hasWeaponRangeToAttack(farAway, 1));
        assertTrue(wraith.canAttackTarget(farAway, true, true, true, 1));
    }

    // =========================================================
    // Health, wounds, shields, energy

    @Test
    public void healthAndWoundCalculations() {
        FakeUnit unit = fake(AUnitType.Terran_Marine);
        int maxHp = unit.maxHp();

        unit.setHp(maxHp);
        assertTrue(unit.isFullyHealthy());
        assertTrue(unit.isHealthy());
        assertFalse(unit.isWounded());
        assertEquals(100, unit.hpPercent());
        assertTrue(unit.hpPercent(100), "hpPercent(n) means 'at least n percent'");
        assertEquals(0, unit.woundHp());
        assertEquals(0.0, unit.woundPercent(), 0.001);

        // A Marine has 40 hit points, so half of it is 20, exactly 50%. The
        // percentages are computed from maxHp here so the assertion pins the
        // formula instead of one unit's numbers.
        int woundedHp = maxHp / 2;
        unit.setHp(woundedHp);
        assertFalse(unit.isFullyHealthy());
        assertTrue(unit.isWounded());
        assertEquals(100 * woundedHp / maxHp, unit.hpPercent());
        assertTrue(unit.hpPercent(100 * woundedHp / maxHp));
        assertFalse(unit.hpPercent(100 * woundedHp / maxHp + 1));
        assertEquals(maxHp - woundedHp, unit.woundHp());
        assertEquals(100.0 * (maxHp - woundedHp) / maxHp, unit.woundPercent(), 0.001);

        unit.setHp(1);
        assertTrue(unit.almostDead());
        assertFalse(unit.isHealthy(), "1 hp is not healthy");
        assertFalse(unit.isFullyHealthy());

        unit.setHp(0);
        assertFalse(unit.isAlive());
        assertTrue(unit.isDead());
    }

    /**
     * The contract for a unit that cannot have shields: the three percentages are
     * {@code NaN}, and <b>every</b> comparison against them is false - so
     * "shieldWound() &lt;= 4" and "shieldWound() &gt;= 40" both read as "this
     * shield-based rule does not apply to me". That is the point of the test: a
     * replacement value (0 or 100) would flip one of those two families on for
     * every Terran and Zerg unit.
     *
     * <p>Pinned deliberately - see the javadoc on {@code AUnit.shieldPercent()}.</p>
     */
    @Test
    public void shieldsOnAUnitThatHasNone() {
        FakeUnit marine = fake(AUnitType.Terran_Marine);

        assertEquals(0, marine.maxShields());
        assertTrue(Double.isNaN(marine.shieldPercent()), "undefined, not 0 and not 100");
        assertTrue(Double.isNaN(marine.shieldWoundPercent()));
        assertTrue(Double.isNaN(marine.shieldWound()));

        assertFalse(marine.shieldWound() <= 4, "'shields nearly intact' must not apply");
        assertFalse(marine.shieldWound() >= 40, "'shields badly wounded' must not apply");
        assertFalse(marine.shieldPercent() >= 100);

        // The boolean accessors stay meaningful, which is why production can use
        // them instead of the percentages.
        assertTrue(marine.shieldHealthy());
        assertFalse(marine.shieldWounded());
    }

    @Test
    public void shieldsWoundLikeHp() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot);

        assertTrue(zealot.shieldHealthy());
        assertEquals(100.0, zealot.shieldPercent(), 0.001);
        assertFalse(zealot.shieldWounded());

        zealot.setShields(zealot.maxShields() / 2);
        assertFalse(zealot.shieldHealthy());
        assertEquals(50.0, zealot.shieldPercent(), 0.001);
        assertTrue(zealot.shieldWounded());
    }

    @Test
    public void energyAndCooldown() {
        FakeUnit vessel = fake(AUnitType.Terran_Science_Vessel);

        vessel.setEnergy(100);
        assertEquals(100, vessel.energy());
        assertTrue(vessel.energy(100));
        assertTrue(vessel.energy(99));
        assertFalse(vessel.energy(101));
    }

    @Test
    public void cooldownBlocksAttackingOnlyWhenAsked() {
        FakeUnit marine = fake(AUnitType.Terran_Marine);
        FakeUnit zergling = fake(AUnitType.Zerg_Zergling, 11);
        int absolute = marine.cooldownAbsolute();

        marine.cooldown = 0;
        assertEquals(0, marine.cooldownRemaining());
        assertEquals(100, marine.cooldownPercent(), "no cooldown means fully ready");
        assertTrue(marine.noCooldown());
        assertFalse(marine.hasCooldown());

        marine.cooldown = absolute / 2;
        assertEquals(absolute / 2, marine.cooldownRemaining());
        assertTrue(marine.cooldownPercent() < 100 && marine.cooldownPercent() > 0,
            "half a cooldown is neither ready nor empty");
        assertFalse(marine.canAttackTarget(zergling, true, true, true, 0),
            "a cooling unit cannot be chosen as an attack target");
        assertTrue(marine.canAttackTarget(zergling, true, true, false, 0),
            "unless the caller passes includeCooldown = false");

        // 4 frames is the documented 'too busy to attack' threshold.
        marine.cooldown = 4;
        assertFalse(marine.canAttackTarget(zergling, true, true, true, 0));
        marine.cooldown = 3;
        assertTrue(marine.canAttackTarget(zergling, true, true, true, 0));

        // Small cooldowns count as "no cooldown" at all.
        marine.cooldown = 2;
        assertTrue(marine.noCooldown());
        assertFalse(marine.hasCooldown());
        marine.cooldown = 3;
        assertFalse(marine.noCooldown());
        assertTrue(marine.hasCooldown());
    }

    // =========================================================
    // Status flags, activity, targeting

    @Test
    public void statusFlags() {
        FakeUnit unit = fake(AUnitType.Terran_Marine);

        unit.setCloaked(true);
        assertTrue(unit.isCloaked());
        unit.setCloaked(false);
        assertFalse(unit.isCloaked());

        unit.setBurrowed(true);
        assertTrue(unit.isBurrowed());
        unit.setBurrowed(false);
        assertFalse(unit.isBurrowed());

        unit.setDetected(true);
        assertTrue(unit.isDetected());
        unit.setDetected(false);
        assertFalse(unit.isDetected());

        unit.setLockedDown(true);
        assertTrue(unit.isLockedDown());
        unit.setLockedDown(false);
        assertFalse(unit.isLockedDown());

        unit.setStasised(true);
        assertTrue(unit.isStasised());
        unit.setStasised(false);
        assertFalse(unit.isStasised());

        unit.setUnderDarkSwarm(true);
        assertTrue(unit.isUnderDarkSwarm());
        assertTrue(unit.isNotAttackableByRangedDueToSpell(),
            "a ranged unit cannot shoot into dark swarm");

        assertFalse(unit.isUnderStorm(), "FakeUnit hardcodes this; needs an engine storm");

        unit.loaded = true;
        assertTrue(unit.isLoaded());
        unit.loaded = false;
        assertFalse(unit.isLoaded());
    }

    @Test
    public void activityStates() {
        FakeUnit unit = fake(AUnitType.Terran_Marine);

        unit.lastCommand = "Move";
        assertTrue(unit.isMoving());
        assertFalse(unit.isAttacking());

        unit.lastCommand = "AttackUnit";
        assertFalse(unit.isMoving());
        assertTrue(unit.isAttacking());

        unit.lastCommand = "Hold";
        assertTrue(unit.isHoldingPosition());

        unit.lastCommand = "Patrolling";
        assertTrue(unit.isPatrolling());
    }

    @Test
    public void immobilisation() {
        FakeUnit vulture = fake(AUnitType.Terran_Vulture);
        assertTrue(vulture.notImmobilized());

        vulture.setLockedDown(true);
        assertFalse(vulture.notImmobilized());

        vulture.setLockedDown(false);
        vulture.setStasised(true);
        assertFalse(vulture.notImmobilized());
    }

    @Test
    public void missionsAndSpeed() {
        FakeUnit vulture = fake(AUnitType.Terran_Vulture);
        FakeUnit scv = fake(AUnitType.Terran_SCV);

        assertTrue(vulture.isQuick(), "Vulture max speed 6.4 >= 5.8");
        assertFalse(scv.isQuick());

        // Missions come from the unit's squad, not from lastCommand.
        assertTrue(vulture.isMissionAttack());
        assertTrue(vulture.isMissionAttackOrGlobalAttack());
        assertFalse(vulture.isMissionDefend());
        assertFalse(vulture.isMissionSparta());
        assertFalse(vulture.isSpecialMission());

        assertEquals(0.0, vulture.speed(), 0.001, "no velocity without an engine");
        assertTrue(vulture.maxSpeed() > scv.maxSpeed());
    }

    @Test
    public void targetingAndFacingTarget() {
        FakeUnit unit = fake(AUnitType.Terran_Marine);
        FakeUnit target = fake(AUnitType.Zerg_Zergling, 12);

        assertTrue(unit.noTarget());
        assertFalse(unit.hasTarget());
        assertNull(unit.target());

        unit.target = target;
        assertFalse(unit.noTarget());
        assertTrue(unit.hasTarget());
        assertSame(target, unit.target());

        unit.targetPosition = target.position();
        assertEquals(target.position(), unit.targetPosition());

        assertTrue(target.isTargetedBy(unit));
        target.target = unit;
        assertTrue(target.isTargetedBy(unit));
    }

    @Test
    public void distancesToTargetAndPosition() {
        FakeUnit unit = fake(AUnitType.Terran_Marine, 10);
        FakeUnit target = fake(AUnitType.Zerg_Zergling, 15);
        unit.target = target;

        assertEquals(5.0, unit.distToTarget(), 0.001);
        assertTrue(unit.distToTargetLessThan(6));
        assertFalse(unit.distToTargetLessThan(4));
        assertTrue(unit.distToTargetMoreThan(4));
        assertFalse(unit.distToTargetMoreThan(6));

        unit.targetPosition = APosition.create(20, 10);
        assertEquals(10.0, unit.distToTargetPosition(), 0.001);
        assertTrue(unit.targetPositionAtLeastAway(9));
        assertFalse(unit.targetPositionAtLeastAway(11));
    }

    @Test
    public void damageAgainstTheTargetType() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);

        assertEquals(marine.groundWeapon(), marine.weaponAgainst(fake(AUnitType.Zerg_Zergling, 14)));
        assertEquals(marine.airWeapon(), marine.weaponAgainst(fake(AUnitType.Zerg_Overlord, 10)));
        assertEquals(6, marine.damageAgainst(fake(AUnitType.Zerg_Zergling, 14)),
            "Marine deals 6 damage");
    }

    @Test
    public void underAttackIsEmptyUntilSomethingAttacks() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10);

        UnderAttack underAttack = dragoon.underAttack();
        assertNull(underAttack.lastBy());
        assertEquals(99999, underAttack.lastAgo(), "99999 is the 'never' sentinel");
    }

    @Test
    public void identityStrings() {
        FakeUnit unit = fake(AUnitType.Terran_Marine, 10);

        assertTrue(unit.idWithHash().contains("#"));
        assertTrue(unit.idWithType().contains("Marine"));
        assertEquals("Marine#" + unit.id(), unit.idWithType());
        assertEquals(unit.idWithType(), unit.typeWithUnitId());
        assertTrue(unit.typeWithUnitId().contains("Marine"));
        assertEquals("NO_COMMAND", unit.lastCommandName());
    }

    @Test
    public void miscProperties() {
        FakeUnit cc = fake(AUnitType.Terran_Command_Center, 10);

        assertEquals(cc.id() % 2 == 0, cc.idIsEven());
        assertEquals(cc.id() % 2 != 0, cc.idIsOdd());

        assertTrue(cc.canLift());
        cc.lifted = true;
        assertTrue(cc.isLifted());
        cc.lifted = false;
        assertFalse(cc.isLifted());

        assertTrue(cc.isPowered(), "FakeUnit defaults to powered");
        assertTrue(cc.hasNoWeaponAtAll(), "a Command Center has no weapon");
        assertFalse(fake(AUnitType.Terran_Bunker, 24).hasNoWeaponAtAll());
        assertFalse(fake(AUnitType.Terran_Marine, 30).hasNoWeaponAtAll());

        cc.enemy = false;
        cc.neutral = true;
        assertTrue(cc.isNeutral());
        assertFalse(cc.isEnemy());

        cc.neutral = false;
        cc.enemy = true;
        assertTrue(cc.isEnemy());
        assertFalse(cc.isOur());

        cc.enemy = false;
        assertTrue(cc.isOur());

        cc.idle = true;
        cc.busy = false;
        assertTrue(cc.isIdle());
        assertFalse(cc.isBusy());

        cc.idle = false;
        cc.busy = true;
        assertTrue(cc.isBusy());
        assertFalse(cc.isIdle());

        cc.completed = true;
        assertTrue(cc.isCompleted());
        cc.completed = false;
        assertFalse(cc.isCompleted());
    }

    // =========================================================
    // Facing geometry

    @Test
    public void facingUsesTheTolerancesOfTheEngine() {
        FakeUnit ours = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit theirs = fake(AUnitType.Zerg_Zergling, 12);
        ours.target = theirs;
        theirs.target = ours;

        // Our unit is due east of theirs, so "east" is facing them.
        ours.setAngle(0);
        theirs.setAngle(0);

        assertTrue(ours.isFacing(theirs), "ours point east and the target is east");
        assertFalse(theirs.isFacing(ours), "theirs point east, but ours is west of them");
        assertTrue(ours.isFacingItsTarget());
        assertFalse(theirs.isFacingItsTarget());

        // Facing them: our unit faces west, away from the target.
        ours.setAngle(Math.PI);
        assertFalse(ours.isFacing(theirs));
        assertFalse(ours.isFacingItsTarget());

        // Perpendicular (90 degrees) is beyond the 1.1 rad tolerance.
        ours.setAngle(Math.PI / 2);
        assertFalse(ours.isFacing(theirs));
        assertFalse(ours.isFacingItsTarget());

        // ...while 30 degrees is inside it.
        ours.setAngle(Math.PI / 6);
        assertTrue(ours.isFacing(theirs), "30 degrees is within the 1.1 rad tolerance");
        assertTrue(ours.isFacingItsTarget());

        // isOtherFacingThisUnit asks whether the *other* unit faces us, so
        // they have to point west (their unit is east of ours).
        ours.setAngle(0);
        theirs.setAngle(Math.PI);
        assertTrue(ours.isOtherFacingThisUnit(theirs));
        theirs.setAngle(Math.PI / 3);
        assertFalse(ours.isOtherFacingThisUnit(theirs), "60 degrees off is outside 1.1 rad");
        theirs.setAngle(Math.PI - Math.PI / 6);
        assertTrue(ours.isOtherFacingThisUnit(theirs), "30 degrees off is inside 1.1 rad");
    }

    /**
     * Both checks ask about the other unit's angle, but with different reference
     * directions, so they answer different questions: "is it facing us?" versus
     * "is it showing its back?". Their unit is east of ours.
     */
    @Test
    public void showingBackIsTheOppositeQuestionToFacingUs() {
        FakeUnit ours = fake(AUnitType.Terran_Marine, 10);
        FakeUnit theirs = fake(AUnitType.Zerg_Zergling, 13);

        theirs.setAngle(Math.PI);
        assertTrue(ours.isOtherFacingThisUnit(theirs), "pointing straight at us");
        assertFalse(ours.isOtherShowingBackToUs(theirs));

        theirs.setAngle(0);
        assertFalse(ours.isOtherFacingThisUnit(theirs));
        assertTrue(ours.isOtherShowingBackToUs(theirs), "pointing straight away from us");

        theirs.setAngle(Math.PI / 6);
        assertTrue(ours.isOtherShowingBackToUs(theirs), "30 degrees off 'away' is inside 0.95 rad");

        theirs.setAngle(Math.PI / 3);
        assertFalse(ours.isOtherShowingBackToUs(theirs), "60 degrees off is outside 0.95 rad");
    }

    @Test
    public void facingHelperAgreesWithTheRawVector() {
        FakeUnit ours = fake(AUnitType.Terran_Marine, 10);
        FakeUnit theirs = fake(AUnitType.Zerg_Zergling, 13);
        // Measured against the raw vectors: "facing us" compares their angle with
        // other -> ours (1.1 rad), "showing back" with ours -> other (0.95 rad).
        double towardsUs = Vectors.directionTowards(theirs.position(), ours.position()).toAngle();
        double awayFromUs = Vectors.directionTowards(ours.position(), theirs.position()).toAngle();

        assertEquals(Math.PI, towardsUs, 0.001,
            "their unit is east of ours, so 'towards us' points west");
        assertEquals(0.0, awayFromUs, 0.001);

        for (double angle : new double[]{0, 0.5, 1.0, 2.0, 3.0, Math.PI}) {
            theirs.setAngle(angle);

            assertEquals(angleDifference(towardsUs, angle) <= 1.1,
                ours.isOtherFacingThisUnit(theirs),
                "facing window (1.1 rad) at " + angle + " rad");
            assertEquals(angleDifference(awayFromUs, angle) <= 0.95,
                ours.isOtherShowingBackToUs(theirs),
                "showing-back window (0.95 rad) at " + angle + " rad");
        }
    }

    // =========================================================
    // "How long ago" timestamps

    @Test
    public void everyTimestampAccessorComparesTheSameWay() {
        FakeUnit unit = fake(AUnitType.Terran_Marine);
        int now = A.now();

        // Each entry is one of the eight accessors that were migrated from
        // AUnit fields into UnitState (Stage E). They all follow the same
        // contract: at exactly N frames ago, "less than N ago" and "more than
        // N ago" are both true; one frame later only "more" is.
        assertTimestampContract("lastPositionChanged",
            ago -> unit.unitState().setLastPositionChanged(now - ago),
            unit::lastPositionChangedLessThanAgo, unit::lastPositionChangedMoreThanAgo);
        assertTimestampContract("lastStartedAttack",
            ago -> unit.unitState().setLastStartedAttack(now - ago),
            unit::lastStartedAttackLessThanAgo, unit::lastStartedAttackMoreThanAgo);
        assertTimestampContract("lastUnderAttack",
            ago -> unit.unitState().setLastUnderAttack(now - ago),
            unit::lastUnderAttackLessThanAgo, unit::lastUnderAttackMoreThanAgo);
        assertTimestampContract("lastAttackFrame",
            ago -> unit.unitState().setLastAttackFrame(now - ago),
            unit::lastAttackFrameLessThanAgo, unit::lastAttackFrameMoreThanAgo);
        assertTimestampContract("lastAttackOrder",
            ago -> unit.unitState().setLastAttackOrder(now - ago),
            unit::lastAttackOrderLessThanAgo, unit::lastAttackOrderMoreThanAgo);
        assertTimestampContract("lastFrameOfStartingAttack",
            ago -> unit.unitState().setLastFrameOfStartingAttack(now - ago),
            unit::lastFrameOfStartingAttackLessThanAgo, unit::lastFrameOfStartingAttackMoreThanAgo);
        assertTimestampContract("lastStartedRunning",
            ago -> unit.unitState().setLastStartedRunning(now - ago),
            unit::lastStartedRunningLessThanAgo, unit::lastStartedRunningMoreThanAgo);
        assertTimestampContract("lastStoppedRunning",
            ago -> unit.unitState().setLastStoppedRunning(now - ago),
            unit::lastStoppedRunningLessThanAgo, unit::lastStoppedRunningMoreThanAgo);
    }

    @Test
    public void combatTimingHistory() {
        FakeUnit unit = fake(AUnitType.Terran_Marine);
        int now = A.now();

        unit.unitState().setLastAttackFrame(now - 10);
        assertTrue(unit.shotAgo(15), "shot 10 frames ago is within 15");
        assertFalse(unit.shotAgo(5), "shot 10 frames ago is not within 5");
        assertTrue(unit.shotSecondsAgo(1), "10 frames is less than one second");
        assertFalse(unit.didntShootRecently(1));

        unit.unitState().setLastAttackFrame(now - 100);
        assertFalse(unit.shotSecondsAgo(1));
        assertTrue(unit.didntShootRecently(1));

        unit.unitState().setLastStartedRunning(now - 10);
        assertTrue(unit.ranRecently(1));
        unit.unitState().setLastStartedRunning(now - 100);
        assertFalse(unit.ranRecently(1));

        unit.lastCommand = "AttackUnit";
        unit.setLastActionReceived(now - 5);
        assertTrue(unit.isAttackingRecently());
        unit.setLastActionReceived(now - 100);
        assertFalse(unit.isAttackingRecently());

        unit.unitState().setLastPositionChanged(now - 5);
        assertEquals(5, unit.lastPositionChangedAgo());
    }

    // =========================================================
    // Queries that need a world

    @Test
    public void nearbyCountsSeeOnlyTheStubs() {
        FakeUnit unit = fake(AUnitType.Terran_Marine, 10);
        FakeUnit friend = fake(AUnitType.Terran_Medic, 11);
        FakeUnit closeEnemy = fake(AUnitType.Zerg_Zergling, 12);
        FakeUnit farEnemy = fake(AUnitType.Zerg_Hydralisk, 15);

        world(1, fakeOurs(unit, friend), fakeEnemies(closeEnemy, farEnemy), () -> {
            assertEquals(0, unit.friendsInRadiusCount(0.9));
            assertEquals(1, unit.friendsInRadiusCount(1));

            assertEquals(0, unit.enemiesNearCount(1.9));
            assertEquals(1, unit.enemiesNearCount(2));
            assertEquals(2, unit.enemiesNearCount(6));

            assertEquals(2.0, unit.nearestEnemyDist(), 0.001);
            assertEquals(2.0, unit.nearestMeleeEnemyDist(), 0.001);

            // Counter-intuitive but measured: a Hydralisk is *ranged* in
            // Atlantis (weapon range 5), so only the zergling counts as
            // melee. The old version of this test assumed the opposite.
            assertEquals(2, unit.enemiesNearCount(6));
            assertEquals(1, unit.rangedEnemiesCount(6), "the hydra is the ranged one");
            assertEquals(0, unit.meleeEnemiesNearCount(1.9));
            assertEquals(1, unit.meleeEnemiesNearCount(2), "the zergling is the melee one");
            assertEquals(1, unit.meleeEnemiesNearCount(6), "the hydra does not count as melee");

            assertEquals(0, unit.allUnitsNear().inRadius(0.9, unit).count());
            assertEquals(1, unit.allUnitsNear().inRadius(1, unit).count());
        });
    }

    @Test
    public void nearbyBuildingsAndBase() {
        FakeUnit probe = fake(Protoss_Probe, 10);

        world(1, fakeOurs(probe, fake(Protoss_Nexus, 2), fake(Protoss_Pylon, 5)), fakeEnemies(fake(AUnitType.Zerg_Zergling, 12)), () -> {
            assertEquals(2.0, probe.distTo(probe.nearestEnemy()), 0.001);
            assertEquals(8.0, probe.distToBase(), 0.001, "Nexus is 8 tiles west");
            assertEquals(5.0, probe.distToBuilding(), 0.001, "Pylon is 5 tiles west");
        });
    }

    @Test
    public void safeFromMeleeWhenNobodyIsInMeleeRange() {
        FakeUnit zealot = fake(Protoss_Zealot, 10);

        world(1, units(zealot), fakeEnemies(fake(AUnitType.Zerg_Zergling, 16)), () -> {
        assertEquals(0, zealot.meleeEnemiesNearCount(3.0), "6 tiles is outside 3");
        assertTrue(zealot.isSafeFromMelee());
        });
    }

    /**
     * A non-dragoon is safe from melee while no melee enemy is within
     * {@code min(3.1, 1.6 or 1.8 + woundPercent)} tiles: 1.6 at full health,
     * 1.8 plus the wounded percentage when below 60 hp. The old test asserted a
     * flat 3 tile radius, which is what made it pass for the wrong reason.
     */
    @Test
    public void unsafeWhenAMeleeUnitIsNextToUs() {
        FakeUnit zealot = fake(Protoss_Zealot, 10);

        world(1, units(zealot), fakeEnemies(fake(AUnitType.Zerg_Zergling, 11)), () -> {
        assertEquals(1, zealot.meleeEnemiesNearCount(1.6), "1 tile is inside 1.6");
        assertFalse(zealot.isSafeFromMelee());
        });
    }

    @Test
    public void meleeSafetyMarginGrowsWithWounds() {
        FakeUnit zealot = fake(Protoss_Zealot, 10);
        FakeUnit zergling = fake(AUnitType.Zerg_Zergling, 11);

        world(1, units(zealot), fakeEnemies(zergling), () -> {
        zealot.setHp(zealot.maxHp());
        assertEquals(0.0, zealot.woundPercent(), 0.001);
        assertFalse(zealot.isSafeFromMelee(), "at full hp the margin is 1.6 tiles");

        // Half hp means half wounded: 1.8 + 50 > 1.6, and the zergling is
        // only 1 tile away, so the wider margin does not save us here.
        zealot.setHp(zealot.maxHp() / 2);
        assertEquals(50.0, zealot.woundPercent(), 0.001);
        assertFalse(zealot.isSafeFromMelee());
        });
    }

    // =========================================================
    // Targeting visibility rules

    @Test
    public void cannotTargetUndetectedUnits() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Drone, 11).setBurrowed(true).setDetected(false),
            fake(AUnitType.Zerg_Lurker, 12).setCloaked(true).setDetected(false),
            fake(AUnitType.Zerg_Lurker, 13).setBurrowed(true).setDetected(false),
            fake(AUnitType.Protoss_Dark_Templar, 11).setCloaked(true).setDetected(false)
        );

        world(1, units(our), enemies, () ->
            assertNull(ATargeting.defineBestEnemyToAttack(our),
                "nothing is detected, so there is no target"));
    }

    // One world per test on purpose: the unit collections are cached per unit
    // and per selection, so building two worlds inside one test method leaks the
    // first one into the second.

    @Test
    public void canTargetDetectedBurrowedDrone() {
        assertOnlyTargetIs(fake(AUnitType.Zerg_Drone, 13).setBurrowed(true).setDetected(true));
    }

    @Test
    public void canTargetDetectedCloakedDragoon() {
        assertOnlyTargetIs(fake(AUnitType.Protoss_Dragoon, 12).setCloaked(true).setDetected(true));
    }

    @Test
    public void canTargetDetectedBurrowedLurker() {
        assertOnlyTargetIs(fake(AUnitType.Zerg_Lurker, 13).setBurrowed(true).setDetected(true));
    }

    @Test
    public void canTargetDetectedCloakedDarkTemplar() {
        assertOnlyTargetIs(fake(AUnitType.Protoss_Dark_Templar, 11).setCloaked(true).setDetected(true));
    }

    @Test
    public void ourAndEnemyFlagsFollowTheTeam() {
        FakeUnit our = fake(AUnitType.Terran_Marine);
        FakeUnit enemy = fakeEnemy(AUnitType.Protoss_Zealot, 16);

        world(1, units(our), new FakeUnit[]{enemy}, () -> {
        assertTrue(our.isOur());
        assertFalse(our.isEnemy());

        assertFalse(enemy.isOur());
        assertTrue(enemy.isEnemy());
        });
    }

    @Test
    public void nearCollectionsSeeTheStubsOnly() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit friend = fake(AUnitType.Terran_Medic, 11);
        FakeUnit enemy = fake(AUnitType.Protoss_Zealot, 12);

        world(1, fakeOurs(our, friend), fakeEnemies(enemy), () -> {
        assertEquals(1, our.enemiesNear().count(), "the zealot is our only enemy");
        assertEquals(1, our.friendsNear().count(), "the medic is our only friend, we are excluded");

        // Seen from the other side: our two units are "its" enemies, and it
        // has no friends here because nothing was ever discovered.
        assertEquals(2, enemy.enemiesNear().count());
        assertEquals(0, enemy.friendsNear().count());
        });
    }

    // =========================================================
    // Helpers

    /**
     * Asserts the shared contract of all eight timestamp accessors: they take
     * the *value* passed to the setter and compare it against "now".
     *
     * @param name       accessor family, for the failure message
     * @param set        writes the timestamp (frames ago)
     * @param lessThanA  {@code x < N} predicate
     * @param moreThanA  {@code x >= N} predicate
     */
    private void assertTimestampContract(
        String name,
        Consumer<Integer> set,
        IntPredicate lessThanA,
        IntPredicate moreThanA
    ) {
        set.accept(5);
        assertTrue(lessThanA.test(6), name + ": 5 ago is less than 6 ago");
        assertTrue(lessThanA.test(5), name + ": 5 ago is less than 5 ago");
        assertFalse(lessThanA.test(4), name + ": 5 ago is not less than 4 ago");
        assertTrue(moreThanA.test(4), name + ": 5 ago is more than 4 ago");
        assertTrue(moreThanA.test(5), name + ": 5 ago is more than 5 ago");
        assertFalse(moreThanA.test(6), name + ": 5 ago is not more than 6 ago");
    }

    private static double angleDifference(double a, double b) {
        double difference = Math.abs(a - b);
        return Math.min(difference, 2 * Math.PI - difference);
    }

    private void assertOnlyTargetIs(FakeUnit expectedTarget) {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit[] ours = fakeOurs(our);
        FakeUnit[] enemies = fakeEnemies(expectedTarget);

        world(1, ours, enemies, () ->
            assertSame(expectedTarget, ATargeting.defineBestEnemyToAttack(our)));
    }
}
