package atlantis.units.select;

import atlantis.units.AUnit;
import atlantis.util.Callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class BaseSelection {

    protected List<AUnit> data;

    /**
     * To cache a value we need all previous filters so in the end it looks like:
     * "our.buildings.inRadius:2,10"
     */
    protected String currentCachePath;

    // =========================================================

    protected BaseSelection(Collection<? extends AUnit> unitsData, String initCachePath) {
        data = new ArrayList<>(unitsData);
        currentCachePath = initCachePath;
    }

    // === Cache ===============================================

    protected String addToCachePath(String method) {
        if (currentCachePath != null) {
            currentCachePath += (currentCachePath.length() > 0 ? "." : "") + method;
        }

        return currentCachePath;
    }

    // === Clone ===============================================

    public Selection clone() {
        return new Selection(data, currentCachePath);
    }

    public Selection cloneByRemovingIf(Predicate<AUnit> newDataPredicate, String cachePathSuffix) {
        List<AUnit> newData = new ArrayList<>(data);
        newData.removeIf(newDataPredicate);

        String newCacheKey = currentCachePath + ":" + cachePathSuffix;
        Selection newSelection = new Selection(newData, newCacheKey);

        Select.cache().set(newCacheKey, 1, newSelection);

        return newSelection;
    }

    public Selection cloneByAdding(Collection<? extends AUnit> addThese, String cachePathSuffix) {
        List<AUnit> newData = new ArrayList<>(data);
        newData.addAll(addThese);
        return new Selection(newData, null);
    }

    // === Util =================================================

    /**
     * Returns those elements of this selections, that are missing in otherSelection.
     */
    public Selection minus(Selection otherSelection) {
        List<AUnit> newData = new ArrayList<>(this.data);
        newData.removeAll(otherSelection.data);

        return new Selection(newData, null);
    }
}
