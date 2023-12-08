package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import bwapi.TechType;

import java.util.HashMap;

public class TerranMedic extends Manager {
    /**
     * Specific units that medics should follow in order to heal them as fast as possible
     * when they get wounded.
     */
    protected static final HashMap<AUnit, AUnit> medicsToAssignments = new HashMap<>();
    protected static final HashMap<AUnit, AUnit> assignmentsToMedics = new HashMap<>();

    // =========================================================

    public TerranMedic(AUnit medic) {
        super(medic);
    }

    // =========================================================

    @Override
    public boolean applies() {
        return unit.isMedic();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ContinueHeal.class,
            HealMostWoundedInRange.class,
            HealAnyWoundedNear.class,
            MedicChokeBlockMoveAway.class,
            MedicChokeBlock.class,
            MedicBodyBlock.class,
            UnitTooCloseToBunker.class,
            TooFarFromNearestInfantry.class,
            MoveAwayMedicFromTanks.class,
            StickToAssignments.class,
            AvoidEnemies.class,
        };
    }
}
