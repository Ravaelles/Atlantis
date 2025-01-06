package atlantis.util.object;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NamespaceAccessibility {

    /**
     * Makes all fields and methods accessible for all classes in the same package as the given class.
     *
     * @param referenceClass The reference class from the target package.
     */
    public static void makeAllAccessibleForNamespace(Class<?> referenceClass) {
//     * @throws ClassNotFoundException If any class cannot be loaded.
//     * @throws IOException            If the package cannot be accessed.
        try {
            // Get the package name of the reference class
            String packageName = referenceClass.getPackage().getName();

            // Find all classes in the package
            List<Class<?>> classes = getClassesInPackage(packageName);

            // Make all fields and methods accessible for each class
            for (Class<?> clazz : classes) {
                Accessibility.makeAllAccessible(clazz);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds all classes in the given package.
     *
     * @param packageName The package name.
     * @return A list of classes in the package.
     * @throws ClassNotFoundException If any class cannot be loaded.
     * @throws IOException            If the package cannot be accessed.
     */
    private static List<Class<?>> getClassesInPackage(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
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

    /**
     * Recursively finds all classes in a directory.
     *
     * @param directory   The directory to search.
     * @param packageName The package name.
     * @return A list of classes in the directory.
     * @throws ClassNotFoundException If any class cannot be loaded.
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }
}
