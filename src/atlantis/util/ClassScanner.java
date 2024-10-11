package atlantis.util;

import atlantis.game.A;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClassScanner {
    public static List<Class<?>> classesExtending(Class ourClass) {
        List<Class<?>> listeners = new ArrayList<>();
//        List<Class<?>> classes = ClassScanner.findClasses(packageName);

        try {
            for (Class<?> clazz : findClasses("atlantis")) {
                // Check if the class is a subclass of Listener
                if (ourClass.isAssignableFrom(clazz) && !clazz.equals(ourClass)) {
                    listeners.add(clazz.asSubclass(ourClass));  // Safe cast to Class<? extends Listener>
                }
            }
        } catch (ClassNotFoundException e) {
            A.printStackTrace("AutoRegisterEventListeners ClassNotFoundException: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            A.printStackTrace("AutoRegisterEventListeners IOException: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return listeners;
    }

    private static List<Class<?>> findClasses(String packageName) throws ClassNotFoundException, IOException {
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
