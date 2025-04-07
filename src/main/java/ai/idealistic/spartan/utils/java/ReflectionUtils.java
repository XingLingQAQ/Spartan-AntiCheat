package ai.idealistic.spartan.utils.java;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@UtilityClass
public class ReflectionUtils {

    public static Class<?> getClass(String s) {
        try {
            return Class.forName(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean classExists(String s) {
        try {
            Class.forName(s);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public static Object getFieldFromJar(String jarFilePath, String className, String fieldName) {
        try {
            // Convert the file path to a URL
            File jarFile = new File(jarFilePath);
            URL jarURL = jarFile.toURI().toURL();

            // Create a URLClassLoader to load the JAR file
            try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL})) {
                // Load the class dynamically
                Class<?> clazz = classLoader.loadClass(className);

                // Get the field from the class
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);  // In case the field is private or protected

                // Return the value of the field (static or instance-based)
                return field.get(null);  // For static fields, pass 'null'
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Object invokeMethodFromJar(String jarFilePath, String className, String methodName,
                                             Class<?>[] parameterTypes, Object[] args) {
        try {
            // Convert the file path to a URL
            File jarFile = new File(jarFilePath);
            URL jarURL = jarFile.toURI().toURL();

            // Create a URLClassLoader to load the JAR file
            try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL})) {
                // Load the class dynamically
                Class<?> clazz = classLoader.loadClass(className);

                // Get the method from the class
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);  // In case the method is private or protected

                // Invoke the method (pass 'null' for static methods or an instance for instance methods)
                // For static methods, use 'null'
                // Return the result (casting to the expected return type)
                return method.invoke(null, args);
            }
        } catch (Exception e) {
            return null;
        }
    }

}
