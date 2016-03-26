package bwapi;

import bwapi.BWEventListener;

/**
 * This class receives all events from Broodwar.
 * To process them, receive an AIModule's instance from {@link Mirror} and call {@link #setEventListener(bwapi.BWEventListener)}
 * to set you own {@link BWEventListener listener}.
 * There's also a stub class ({@link DefaultBWListener}) provided, so you don't have to implement all of the methods.
 */
public class AIModule {

    AIModule(){}

    private BWEventListener eventListener;

    public void setEventListener(BWEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void onStart() {
        if (eventListener != null) {
            eventListener.onStart();
        }
    }

    public void onEnd(boolean isWinner) {
        if (eventListener != null) {
            eventListener.onEnd(isWinner);
        }
    }

    public void onFrame() {
        if (eventListener != null) {
            eventListener.onFrame();
        }
    }

    public void onSendText(String text) {
        if (eventListener != null)
        {
            eventListener.onSendText(text);
        }
    }

    public void onReceiveText(Player player, String text) {
        if (eventListener != null) {
            eventListener.onReceiveText(player, text);
        }
    }

    public void onPlayerLeft(Player player) {
        if (eventListener != null) {
            eventListener.onPlayerLeft(player);
        }
    }

    public void onNukeDetect(Position target) {
        if (eventListener != null) {
            eventListener.onNukeDetect(target);
        }
    }

    public void onUnitDiscover(Unit unit) {
        if (eventListener != null) {
            eventListener.onUnitDiscover(unit);
        }
    }

    public void onUnitEvade(Unit unit) {
        if (eventListener != null) {
            eventListener.onUnitEvade(unit);
        }
    }

    public void onUnitShow(Unit unit) {
        if (eventListener != null) {
            eventListener.onUnitShow(unit);
        }
    }

    public void onUnitHide(Unit unit) {
        if (eventListener != null) {
            eventListener.onUnitHide(unit);
        }
    }

    public void onUnitCreate(Unit unit) {
        if (eventListener != null) {
            eventListener.onUnitCreate(unit);
        }
    }

    public void onUnitDestroy(Unit unit) {
        if (eventListener != null) {
            eventListener.onUnitDestroy(unit);
        }
    }

    public void onUnitMorph(Unit unit) {
        if (eventListener != null) {
            eventListener.onUnitMorph(unit);
        }
    }

    public void onUnitRenegade(Unit unit) {
        if (eventListener != null) {
            eventListener.onUnitRenegade(unit);
        }
    }

    public void onSaveGame(String gameName) {
        if (eventListener != null) {
            eventListener.onSaveGame(gameName);
        }
    }

    public void onUnitComplete(Unit unit) {
        if (eventListener != null) {
            eventListener.onUnitComplete(unit);
        }
    }

    public void onPlayerDropped(Player player) {
        if (eventListener != null) {
            eventListener.onPlayerDropped(player);
        }
    }

}
