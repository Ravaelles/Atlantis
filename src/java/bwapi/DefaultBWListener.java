package bwapi;

import bwapi.BWEventListener;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;

/**
 * A utility stub class providing a default implementation of {@link BWEventListener},
 * override it's methods if you want to handle only some events.
 */
public class DefaultBWListener implements BWEventListener {

    @Override
    public void onStart() {

    }

    @Override
    public void onEnd(boolean b) {

    }

    @Override
    public void onFrame() {

    }

    @Override
    public void onSendText(String s) {

    }

    @Override
    public void onReceiveText(Player player, String s) {

    }

    @Override
    public void onPlayerLeft(Player player) {

    }

    @Override
    public void onNukeDetect(Position position) {

    }

    @Override
    public void onUnitDiscover(Unit unit) {

    }

    @Override
    public void onUnitEvade(Unit unit) {

    }

    @Override
    public void onUnitShow(Unit unit) {

    }

    @Override
    public void onUnitHide(Unit unit) {

    }

    @Override
    public void onUnitCreate(Unit unit) {

    }

    @Override
    public void onUnitDestroy(Unit unit) {

    }

    @Override
    public void onUnitMorph(Unit unit) {

    }

    @Override
    public void onUnitRenegade(Unit unit) {

    }

    @Override
    public void onSaveGame(String s) {

    }

    @Override
    public void onUnitComplete(Unit unit) {

    }

    @Override
    public void onPlayerDropped(Player player) {

    }
}
