package atlantis.combat.missions;

import atlantis.architecture.Commander;

public class MissionHelper extends Commander {
    public boolean isMissionDefend() {
        return this.equals(Missions.DEFEND);
    }

    public boolean isMissionSparta() {
        return this.equals(Missions.SPARTA);
    }

    public boolean isMissionDefendOrSparta() {
        return isMissionSparta() || isMissionDefend();
    }

    public boolean isMissionAttack() {
        return this.equals(Missions.ATTACK);
    }
}
