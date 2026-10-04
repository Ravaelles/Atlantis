package tests.e2e;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Options;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The stub world deals damage, in a second instead of a minute.
 *
 * <p>Both rush scenarios used to carry a negative control: the same base with no
 * defence, losing to the same rush, in its own 900-frame run. Two of those cost
 * 70 s of the scenario tier's runtime and asserted one property - that
 * {@link ScenarioCombat} resolves combat at all. Without it a stub world that
 * dealt no damage would make the defended scenarios pass <b>vacuously</b>: the
 * base would survive because nothing was hitting it.</p>
 *
 * <p>So the property is asserted directly, and cheaply:</p>
 * <ul>
 *   <li>two units in reach trade damage, and a unit out of reach does not;</li>
 *   <li>weapon cooldowns are respected - a marine's 15-frame cooldown means
 *       frame 2 is not a second free shot;</li>
 *   <li>a dead unit deals nothing;</li>
 *   <li>the world drops dead units from its unit lists, which several
 *       {@code Select} builders take on trust instead of re-checking
 *       {@code isAlive()} - that is what let a probe spend frames attacking a
 *       corpse in the B-19 scenario.</li>
 * </ul>
 *
 * <p>The damage numbers are engine data (marine 6, zergling 5, both at 15
 * frames), so a jar swap that changed a weapon would fail here loudly.</p>
 */
public class StubWorldDamageTest extends WorldStubForTests {

    @Test
    public void unitsInReachTradeDamageAndOutOfReachUnitsDoNot() {
        // Half a tile apart: zergling claws reach 1 tile in the engine, which the
        // bot scores as 0 whole tiles, and ScenarioCombat adds half a tile of
        // slack. A full tile away would be out of reach, which is the point of
        // the third unit.
        FakeUnit marine = new FakeUnit(AUnitType.Terran_Marine, 10, 10);
        FakeUnit zergling = new FakeUnit(AUnitType.Zerg_Zergling, 10.5, 10);
        FakeUnit faraway = new FakeUnit(AUnitType.Zerg_Zergling, 30, 10);

        int marineHp = marine.hp();
        int lingHp = zergling.hp();
        int farawayHp = faraway.hp();

        ScenarioCombat combat = new ScenarioCombat();
        List<FakeUnit> ours = Collections.singletonList(marine);
        List<FakeUnit> enemies = Arrays.asList(zergling, faraway);

        // Both sides have to shoot *at* something for damage to happen:
        // ScenarioCombat shoots what a unit was told to shoot, except for
        // buildings, which auto-acquire the way the engine does.
        marine.attackUnit(zergling);
        zergling.attackUnit(marine);

        combat.onFrame(1, ours, enemies);

        assertEquals(marineHp - 5, marine.hp(), "a zergling's shot is 5 damage");
        assertEquals(lingHp - 6, zergling.hp(), "a marine's shot is 6 damage");
        assertEquals(farawayHp, faraway.hp(), "a zergling 20 tiles away is out of every reach here");
    }

    @Test
    public void weaponCooldownsAreRespected() {
        FakeUnit marine = new FakeUnit(AUnitType.Terran_Marine, 10, 10);
        FakeUnit zergling = new FakeUnit(AUnitType.Zerg_Zergling, 10.5, 10);

        ScenarioCombat combat = new ScenarioCombat();
        marine.attackUnit(zergling);
        zergling.attackUnit(marine);

        combat.onFrame(1, Collections.singletonList(marine), Collections.singletonList(zergling));
        int afterFirstShot = zergling.hp();

        // Frame 2 is inside the 15-frame cooldown of both weapons.
        combat.onFrame(2, Collections.singletonList(marine), Collections.singletonList(zergling));
        assertEquals(afterFirstShot, zergling.hp(), "no second shot inside the cooldown");

        // Frame 16 is outside it.
        combat.onFrame(16, Collections.singletonList(marine), Collections.singletonList(zergling));
        assertTrue(zergling.hp() < afterFirstShot, "the next cooldown has come round");
    }

    @Test
    public void aDeadUnitDealsNothing() {
        FakeUnit marine = new FakeUnit(AUnitType.Terran_Marine, 10, 10);
        FakeUnit zergling = new FakeUnit(AUnitType.Zerg_Zergling, 10.5, 10);

        zergling.setHp(0);
        marine.attackUnit(zergling);
        zergling.attackUnit(marine);

        int marineHp = marine.hp();
        ScenarioCombat combat = new ScenarioCombat();
        combat.onFrame(1, Collections.singletonList(marine), Collections.singletonList(zergling));

        assertEquals(marineHp, marine.hp(), "a dead attacker deals nothing");
        assertEquals(0, combat.strikesBy(zergling));
    }

    @Test
    public void deadUnitsLeaveTheWorldUnitLists() {
        FakeUnit[] ours = {
            fake(AUnitType.Terran_Marine, 10),
            fake(AUnitType.Terran_Marine, 11),
        };
        FakeUnit[] enemies = {fake(AUnitType.Zerg_Zergling, 20)};

        options = Options.create().set("supplyUsed", 8);

        // Two frames: Select is cached per frame, so the kill has to happen in
        // frame 1 and be observed in frame 2 - exactly like the game, where a
        // unit dies during one frame and is absent from the next frame's lists.
        world(2, ours, enemies, () -> {
            if (A.now() == 1) {
                assertEquals(2, Select.our().size());
                assertEquals(3, Select.all().size());

                ours[1].setHp(0);
            }
            else {
                // Read inside the world: the harness closes its BaseSelect mock
                // when the loop ends, so Select cannot be asked anything after it.
                assertEquals(1, Select.our().size(),
                    "a dead unit is gone from Select.our() - the engine drops it, and "
                        + "several builders rely on that instead of re-checking isAlive()");
                assertEquals(2, Select.all().size(), "and from Select.all() too");
            }
        });
    }
}