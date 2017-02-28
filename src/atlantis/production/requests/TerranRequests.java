package atlantis.production.requests;

import atlantis.production.ADynamicConstructionManager;
import atlantis.AGame;
import atlantis.constructing.AConstructionManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranRequests extends ARequests {

    @Override
    public void requestDetectorQuick(APosition where) {
        int turrets = AConstructionManager.countOurBuildingsFinishedAndPlanned(AUnitType.Terran_Missile_Turret);
        int comsats = AConstructionManager.countOurBuildingsFinishedAndPlanned(AUnitType.Terran_Comsat_Station);
    }

    @Override
    public void requestAntiAirQuick(APosition where) {
        
    }
    
}
