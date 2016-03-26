package bwapi;

import bwapi.AIModule;
import bwapi.BWEventListener;

import java.io.*;
import java.io.File;
import java.lang.Exception;
import java.lang.UnsupportedOperationException;
import java.util.*;
import java.util.zip.*;

/**
 * <p>The API entry point. Standard use case:</p>
 * <ul>
 *     <li>Create a Mirror object and use {@link #getModule()} and then set an {@link AIModule}'s {@link BWEventListener}<br/>
 *     <li>Call {@link #startGame()} to init the API and connect to Broodwar, then launch Broodwar from ChaosLauncher.</li>
 *     <li>In you {@link BWEventListener#onStart()} method, receive the Game object by calling {@link #getGame()}</li>
 * </ul>
 * <br/>
 * <b>Example</b>
 * <pre>
 *     {@code
 *
 *     mirror.getModule().setEventListener(new DefaultBWListener()
 *     {
 *            public void onStart() {
 *                game = mirror.getGame();
 *                self = game.self();
 *                //initialization
 *                ....
 *            }
 *
 *           public void onUpdate() {
 *               for (Unit myUnit : self.getUnits()) {
 *                   //give orders to unit
 *                   ...
 *                }
 *           }
 *        });
 *     }
 *     mirror.startGame();
 * </pre>

 * <p><b>Note:</b> The Game object is initialized during the {@link #startGame()} as well as other BWMirror API's constants.
 * Do not use any API releated methods/fields prior to {@link #startGame()}.</p>

 */
public class Mirror {

    private Game game;

    private AIModule module = new AIModule();

    private FrameCallback frameCallback;

    private static final boolean EXTRACT_JAR = true;

    private static final String VERSION = "2_5";

    static {
        String arch = System.getProperty("os.arch");
        String dllNames[] = {"bwapi_bridge" + VERSION, "libgmp-10", "libmpfr-4"};
        if(!arch.equals("x86")){
            throw new UnsupportedOperationException("BWMirror API supports only x86 architecture.");
        }
        try {
            if (EXTRACT_JAR) {
                System.setProperty("java.library.path", ".");
                java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                fieldSysPath.setAccessible(true);
                fieldSysPath.set(null, null);

                String path = Mirror.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                String decodedPath = java.net.URLDecoder.decode(path, "UTF-8");

                for (String dllName : dllNames) {
                    String dllNameExt = dllName + ".dll";
                    if (!new File(dllNameExt).exists()) {
                        JarResources jar = new JarResources(path);
                        byte[] correctDllData = jar.getResource(dllNameExt);
                        FileOutputStream funnyStream = new FileOutputStream(dllNameExt);
                        funnyStream.write(correctDllData);
                        funnyStream.close();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to extract native libraries.\n" + e);
        }

        System.loadLibrary(dllNames[0]);

        File dataDir = new File("bwapi-data/BWTA");
        if(!dataDir.exists()){
            try {
                dataDir.mkdirs();
            } catch (Exception e) {
                System.err.println("Unable to create /bwapi-data/BWTA folder, BWTA analysis will not be saved.");
            }
        }
    }

    public Game getGame() {
        return game;
    }

    public AIModule getModule() {
        return module;
    }

    /**
     * Starts the API, initializes all constants ( {@link UnitType}, {@link WeaponType} ) and the {@link Game} object.
     * This method blocks until the end of game.
     */
    public native void startGame();

    private void update() {
        if (frameCallback != null) {
            frameCallback.update();
        }
    }

    /*public void setFrameCallback(bwapi.Mirror.FrameCallback frameCallback) {
        this.frameCallback = frameCallback;
    } */

    /**
     * The simplest interface to receive update event from Broodwar. The {@link #update()} method is called once each frame.
     * For a simple bot and implementation of this interface is enough, to receive all in game events, implement {@link BWEventListener}.
     */
    /*public*/ private interface FrameCallback {
        public void update();
    }

    @SuppressWarnings({"unchecked"})
    private static class JarResources {

        // external debug flag
        public boolean debugOn = false;

        // jar resource mapping tables
        private Hashtable htSizes = new Hashtable();
        private Hashtable htJarContents = new Hashtable();

        // a jar file
        private String jarFileName;

        /**
         * creates a javabot.JarResources. It extracts all resources from a Jar
         * into an internal hashtable, keyed by resource names.
         *
         * @param jarFileName a jar or zip file
         */
        public JarResources(String jarFileName) {
            this.jarFileName = jarFileName;
            init();
        }

        /**
         * Extracts a jar resource as a blob.
         *
         * @param name a resource name.
         */
        public byte[] getResource(String name) {
            return (byte[]) htJarContents.get(name);
        }

        /**
         * initializes internal hash tables with Jar file resources.
         */
        private void init() {
            try {
                // extracts just sizes only.
                ZipFile zf = new ZipFile(jarFileName);
                Enumeration e = zf.entries();
                while (e.hasMoreElements()) {
                    ZipEntry ze = (ZipEntry) e.nextElement();
                    if (debugOn) {
                        System.out.println(dumpZipEntry(ze));
                    }
                    htSizes.put(ze.getName(), new Integer((int) ze.getSize()));
                }
                zf.close();

                // extract resources and put them into the hashtable.
                FileInputStream fis = new FileInputStream(jarFileName);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipInputStream zis = new ZipInputStream(bis);
                ZipEntry ze = null;
                while ((ze = zis.getNextEntry()) != null) {
                    if (ze.isDirectory()) {
                        continue;
                    }
                    if (debugOn) {
                        System.out.println(
                                "ze.getName()=" + ze.getName() + "," + "getSize()=" + ze.getSize()
                        );
                    }
                    int size = (int) ze.getSize();
                    // -1 means unknown size.
                    if (size == -1) {
                        size = ((Integer) htSizes.get(ze.getName())).intValue();
                    }
                    byte[] b = new byte[(int) size];
                    int rb = 0;
                    int chunk = 0;
                    while (((int) size - rb) > 0) {
                        chunk = zis.read(b, rb, (int) size - rb);
                        if (chunk == -1) {
                            break;
                        }
                        rb += chunk;
                    }
                    // add to internal resource hashtable
                    htJarContents.put(ze.getName(), b);
                    if (debugOn) {
                        System.out.println(
                                ze.getName() + "  rb=" + rb +
                                        ",size=" + size +
                                        ",csize=" + ze.getCompressedSize()
                        );
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("done.");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Dumps a zip entry into a string.
         *
         * @param ze a ZipEntry
         */
        private String dumpZipEntry(ZipEntry ze) {
            StringBuffer sb = new StringBuffer();
            if (ze.isDirectory()) {
                sb.append("d ");
            } else {
                sb.append("f ");
            }
            if (ze.getMethod() == ZipEntry.STORED) {
                sb.append("stored   ");
            } else {
                sb.append("defalted ");
            }
            sb.append(ze.getName());
            sb.append("\t");
            sb.append("" + ze.getSize());
            if (ze.getMethod() == ZipEntry.DEFLATED) {
                sb.append("/" + ze.getCompressedSize());
            }
            return (sb.toString());
        }


    }
}