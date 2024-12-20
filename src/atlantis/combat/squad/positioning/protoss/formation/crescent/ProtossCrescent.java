package atlantis.combat.squad.positioning.protoss.formation.crescent;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.protoss.formation.ProtossShouldCreateFormation;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class ProtossCrescent extends Manager {
    protected static AUnit leader;
    protected static HasPosition conventionalPoint;
    protected static double preferredDistToConventionalPoint;
    protected static double distToConventionalPoint;

    public ProtossCrescent(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isReaver()) return false;
        if (unit.type().isTransport()) return false;

        if (unit.enemiesNear().inRadius(8, unit).notEmpty()) return false;

        leader = unit.squadLeader();
        if (leader == null) return false;

        conventionalPoint = conventionalPoint();
        if (conventionalPoint == null) return false;

        return ProtossShouldCreateFormation.check(unit);
    }

    private static HasPosition conventionalPoint() {
        APosition enemyLocation = EnemyInfo.enemyLocationOrGuess();
        if (enemyLocation != null) return enemyLocation;

        return Select.enemy().groundUnits().nearestTo(leader);
    }

    @Override
    protected Manager handle() {
//        System.err.println(unit + " prefDist = " + A.digit(preferredDistToConventionalPoint));
        preferredDistToConventionalPoint = preferredDist();

//        if (isTooClose() && whenTooClose()) return usedManager(this, "CrescentTooClose");

        return handleSubmanagers();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossCrescentFriendTooFar.class,
            ProtossCrescentTooAhead.class,
            ProtossCrescentTooBehind.class,
        };
    }

//    private boolean isTooClose() {
//        distToConventionalPoint = unit.groundDist(conventionalPoint);
//
//        return distToConventionalPoint < preferredDistToConventionalPoint;
//    }
//
//    private boolean whenTooClose() {
//    }

    private double preferredDist() {
        return leader.groundDist(conventionalPoint);
    }
}
