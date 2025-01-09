package atlantis.information.strategy.response.terran;

import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.reinforce.terran.turrets.TerranMissileTurret;
import atlantis.production.dynamic.reinforce.terran.turrets.TerranSecureBaseWithTurrets;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class TerranAirDefence {
    public static void update() {
        TerranSecureBaseWithTurrets.secureAllBases();
    }
}
