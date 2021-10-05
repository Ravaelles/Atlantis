package atlantis.information;

import atlantis.units.AUnit;
import java.util.ArrayList;


public class AOurUnitsExtraInfo {
    
    public static ArrayList<Integer> idsOfOurDestroyedUnits = new ArrayList<>();
    
    // =========================================================
    
    public static boolean hasOurUnitBeenDestroyed(AUnit unit) {
        return idsOfOurDestroyedUnits.contains(unit.getID());
    }
    
}
