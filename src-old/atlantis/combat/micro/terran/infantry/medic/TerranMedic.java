package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossAvoidCriticalUnits::new,
            MedicAvoidWhenAttacked::new,
            ContinueHeal::new,
            HealMostWoundedInRange::new,
            HealAnyWoundedNear::new,
            MedicChokeBlockMoveAway::new,
            MedicChokeBlock::new,
            MedicBodyBlock::new,
            UnitTooCloseToBunker::new,
            TerranTooFarFromSquadCenter::new,
            TooFarFromNearestInfantry::new,
            MoveAwayMedicFromTanks::new,
            GlueToAssignments::new,
            ProtossAvoidEnemies::new,
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
