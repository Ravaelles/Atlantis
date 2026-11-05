package tests.unit.sim;

import atlantis.map.snapshot.MapSnapshot;
import tests.fakes.FakeUnit;
import tests.fakes.SnapshotTerrain;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * Opt-in live view for StarEngine sim runs (owner order 2026-11-06):
 * one Swing window showing the TauCross snapshot (walkable ground,
 * minerals, geysers) plus every sim unit - our buildings as labeled
 * boxes (orange = still warping, cyan = done), bases ringed, workers
 * as dots (teal = mining, white = doing something else, with the
 * current action printed next to it), enemies red. Enabled per test
 * with {@code SimConfig.withVisualization()} (or
 * {@code -Datlantis.visualize=true} for one ad-hoc run); headless
 * environments silently skip it. The test never waits for the window:
 * {@link #finish()} keeps the last frame on screen for a few seconds
 * and then disposes it, letting the JVM exit.
 *
 * <p>Test-only harness. Production code must never reference it.
 */
public final class SimVisualizer {

    /** Milliseconds the final frame stays visible after the test ends. */
    private static final int HOLD_MS = 5000;

    private static final Object LOCK = new Object();
    private static JFrame frame;
    private static ViewPanel panel;
    private static boolean finishing;

    private SimVisualizer() {
    }

    /**
     * One sim step painted, if visualization is on. Never throws into
     * the sim: a dead window, a closed display, anything - the test
     * owns the verdict, this owns pixels.
     */
    public static void frame(SimDriver driver) {
        if (driver == null || driver.config() == null || !driver.config().visualize()) return;
        if (java.awt.GraphicsEnvironment.isHeadless()) return;

        final ViewModel view;
        try {
            view = snapshot(driver);
        } catch (Exception | Error ignored) {
            return;
        }

        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        ensureWindow(view);
                        panel.setView(view);
                    } catch (Exception | Error ignored) {
                    }
                }
            });
        } catch (Exception | Error ignored) {
        }
    }

    /**
     * Called when the sim ends (SimDriver.close): hold the last frame,
     * then dispose the window so the JVM can exit.
     */
    public static void finish() {
        synchronized (LOCK) {
            if (frame == null || finishing) return;
            finishing = true;
        }

        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Timer closer = new Timer(HOLD_MS, new java.awt.event.ActionListener() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                try {
                                    frame.dispose();
                                } finally {
                                    synchronized (LOCK) {
                                        frame = null;
                                        panel = null;
                                        finishing = false;
                                    }
                                }
                            }
                        });
                        closer.setRepeats(false);
                        closer.start();
                    } catch (Exception | Error ignored) {
                        synchronized (LOCK) {
                            frame = null;
                            panel = null;
                            finishing = false;
                        }
                    }
                }
            });
        } catch (Exception | Error ignored) {
            synchronized (LOCK) {
                frame = null;
                panel = null;
                finishing = false;
            }
        }
    }

    private static ViewModel snapshot(SimDriver driver) {
        ViewModel out = baseView(driver.map().snap, "sim f" + driver.frame());

        for (FakeUnit u : driver.units().ours()) {
            if (u == null || !u.isAlive()) continue;
            out.ours.add(describe(u));
        }
        for (FakeUnit u : driver.units().enemies()) {
            if (u == null || !u.isAlive()) continue;
            out.foes.add(describe(u));
        }

        return out;
    }

    /**
     * One-shot view for stub-world tests (no step loop): draws the given
     * snapshot plus the given units once. Slots come from the global
     * plan exactly like in the sim. No-op without display or on any
     * error; pair with {@link #finish()} to auto-close.
     */
    public static void show(MapSnapshot snap, String title, java.util.List<FakeUnit> ours) {
        if (snap == null || java.awt.GraphicsEnvironment.isHeadless()) return;
        if (!java.lang.Boolean.getBoolean("atlantis.visualize")) return;

        final ViewModel out = baseView(snap, title);
        if (ours != null) {
            for (FakeUnit u : ours) {
                if (u == null || !u.isAlive()) continue;
                out.ours.add(describe(u));
            }
        }

        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        ensureWindow(out);
                        panel.setView(out);
                    } catch (Exception | Error ignored) {
                    }
                }
            });
        } catch (Exception | Error ignored) {
        }
    }

    private static ViewModel baseView(MapSnapshot snap, String title) {
        ViewModel out = new ViewModel();
        out.title = snap.map + " " + title;
        out.mapW = snap.w;
        out.mapH = snap.h;

        for (int ty = 0; ty < out.mapH; ty += 2) {
            for (int tx = 0; tx < out.mapW; tx += 2) {
                char c = tileChar(snap, tx, ty);
                if (c == 'x') continue;
                out.ground.add(new int[]{tx, ty, c == '.' ? 1 : 0});
            }
        }
        for (MapSnapshot.Res res : snap.resources) {
            out.minerals.add(new int[]{res.x, res.y, "G".equals(res.t) ? 1 : 0});
        }
        for (atlantis.placement.core.ReservedSlots.Slot slot :
            atlantis.placement.core.ReservedSlots.slots()) {
            if (slot == null || slot.tile == null || slot.type == null) continue;
            SlotMark mark = new SlotMark();
            mark.tx = slot.tile.tx();
            mark.ty = slot.tile.ty();
            mark.w = slot.type.getTilesWidth();
            mark.h = slot.type.getTilesHeights();
            mark.label = slot.label == null ? "" : slot.label;
            out.slots.add(mark);
        }

        return out;
    }

    private static char tileChar(MapSnapshot snap, int tx, int ty) {
        if (ty < 0 || ty >= snap.h) return 'x';
        String row = snap.tiles.get(ty);
        if (tx < 0 || tx >= row.length()) return 'x';

        return row.charAt(tx);
    }

    private static UnitMark describe(FakeUnit u) {
        UnitMark mark = new UnitMark();
        mark.tx = u.tx();
        mark.ty = u.ty();
        mark.name = u.type() == null ? "?" : u.type().name().replace("Protoss_", "").replace("Zerg_", "");
        mark.building = u.isABuilding();
        mark.base = u.isABuilding() && u.type() != null && u.type().isBase();
        mark.completed = u.isCompleted();
        try {
            mark.gathering = u.isGatheringMinerals() || u.isGatheringGas();
        } catch (Exception | Error ignored) {
            mark.gathering = false;
        }
        try {
            mark.action = u.action() == null ? "" : u.action().name();
        } catch (Exception | Error ignored) {
            mark.action = "";
        }

        return mark;
    }

    private static void ensureWindow(ViewModel view) {
        if (frame != null) {
            frame.setTitle(view.title);
            return;
        }

        panel = new ViewPanel();
        frame = new JFrame(view.title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static final class ViewModel {
        String title = "sim";
        int mapW = 128;
        int mapH = 128;
        final List<int[]> ground = new ArrayList<int[]>();
        final List<int[]> minerals = new ArrayList<int[]>();
        final List<SlotMark> slots = new ArrayList<SlotMark>();
        final List<UnitMark> ours = new ArrayList<UnitMark>();
        final List<UnitMark> foes = new ArrayList<UnitMark>();
    }

    private static final class SlotMark {
        int tx;
        int ty;
        int w;
        int h;
        String label = "";
    }

    private static final class UnitMark {
        int tx;
        int ty;
        String name = "?";
        boolean building;
        boolean base;
        boolean completed;
        boolean gathering;
        String action = "";
    }

    private static final class ViewPanel extends JPanel {
        private static final int MAX_PX = 860;

        private volatile ViewModel view = new ViewModel();

        void setView(ViewModel view) {
            this.view = view;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            int scale = Math.max(1, MAX_PX / Math.max(view.mapW, view.mapH));
            return new Dimension(view.mapW * scale, view.mapH * scale + 24);
        }

        private int scale() {
            return Math.max(1, MAX_PX / Math.max(view.mapW, view.mapH));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            ViewModel v = view;
            int s = scale();

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            for (int[] tile : v.ground) {
                g.setColor(tile[2] == 1 ? new Color(38, 38, 44) : new Color(20, 20, 26));
                g.fillRect(tile[0] * s, tile[1] * s, s * 2 - 1, s * 2 - 1);
            }

            g.setColor(Color.GREEN);
            for (int[] res : v.minerals) {
                if (res[2] == 0) g.setColor(Color.GREEN);
                else g.setColor(Color.CYAN);
                g.fillRect(res[0] * s - 1, res[1] * s - 1, 5, 5);
            }

            g.setColor(Color.WHITE);
            g.drawString(v.title, 6, v.mapH * s + 16);

            for (SlotMark slot : v.slots) {
                boolean taken = false;
                for (UnitMark u : v.ours) {
                    if (!u.building) continue;
                    if (slot.tx < u.tx + 4 && u.tx < slot.tx + slot.w
                        && slot.ty < u.ty + 4 && u.ty < slot.ty + slot.h) {
                        taken = true;
                        break;
                    }
                }
                g.setColor(taken ? Color.GREEN : Color.GRAY);
                g.drawRect(slot.tx * s, slot.ty * s, slot.w * s, slot.h * s);
                g.setColor(Color.WHITE);
                g.drawString(slot.label, slot.tx * s + 2, slot.ty * s + 10);
            }

            for (UnitMark foe : v.foes) {
                g.setColor(Color.RED);
                g.fillOval(foe.tx * s - 2, foe.ty * s - 2, 5, 5);
                g.drawString(foe.name, foe.tx * s + 4, foe.ty * s);
            }

            for (UnitMark u : v.ours) {
                if (u.building) {
                    int w = u.base ? 4 * s : 3 * s;
                    int h = u.base ? 3 * s : 2 * s;
                    g.setColor(u.completed ? Color.CYAN : Color.ORANGE);
                    g.drawRect(u.tx * s, u.ty * s, w, h);
                    g.setColor(Color.WHITE);
                    g.drawString(u.name + " [" + u.tx + "," + u.ty + "]", u.tx * s, u.ty * s - 2);
                    if (u.base) {
                        g.setColor(Color.YELLOW);
                        g.drawRect(u.tx * s - 2, u.ty * s - 2, w + 4, h + 4);
                    }
                }
                else if (u.gathering) {
                    g.setColor(new Color(45, 180, 180));
                    g.fillOval(u.tx * s - 2, u.ty * s - 2, 5, 5);
                }
                else {
                    g.setColor(Color.WHITE);
                    g.fillOval(u.tx * s - 2, u.ty * s - 2, 5, 5);
                    String label = u.name + " " + u.action;
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawString(label.trim(), u.tx * s + 4, u.ty * s);
                }
            }
        }
    }
}
