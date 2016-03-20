package atlantis.combat.micro;

import atlantis.combat.micro.terran.TerranMedic;
import atlantis.AtlantisGame;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.wrappers.SelectUnits;
import atlantis.wrappers.Units;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * Default micro manager that will be used for all melee units.
 */
public class DefaultMeleeManager extends MicroMeleeManager {

    @Override
    public boolean update(Unit unit) {
        System.err.println("##########################################");
        System.err.println("## Currently this is inactive ############");
        System.err.println("## Class: DefaultRangedManager ###########");
        System.err.println("## is the only active micro manager ######");
        System.err.println("##########################################");
        System.exit(-1);
        return false;
    }

}
