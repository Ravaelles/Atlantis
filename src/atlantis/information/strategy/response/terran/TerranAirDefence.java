package atlantis.information.strategy.response.terran;

import atlantis.production.dynamic.terran.turrets.TerranSecureBaseWithTurrets;

public class TerranAirDefence {
    public static void update() {
        TerranSecureBaseWithTurrets.secureAllBases();
    }
}
