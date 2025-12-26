package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.combat.micro.avoid.special.protoss.ProtossAvoidCriticalUnits;
import atlantis.combat.squad.positioning.terran.TerranTooFarFromSquadCenter;
import atlantis.units.AUnit;

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
            ProtossAvoidCriticalUnits.class,
            MedicAvoidWhenAttacked.class,
            ContinueHeal.class,
            HealMostWoundedInRange.class,
            HealAnyWoundedNear.class,
            MedicChokeBlockMoveAway.class,
            MedicChokeBlock.class,
            MedicBodyBlock.class,
            UnitTooCloseToBunker.class,
            TerranTooFarFromSquadCenter.class,
            TooFarFromNearestInfantry.class,
            MoveAwayMedicFromTanks.class,
            GlueToAssignments.class,
            ProtossAvoidEnemies.class,
        };
    }

    public static boolean isAnyMedicAssignedTo(AUnit target) {
        return medicsToAssignments.containsValue(target);
    }

    public static boolean isAnyCloseMedicAssignedTo(AUnit target) {
        AUnit medic = assignmentsToMedics.get(target);

        return medic != null && medic.distToLessThan(target, 2);
    }
}
