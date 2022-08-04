package microsim.gui.probe;

import java.lang.reflect.*;

/**
 * A collection of static methods using the java reflection to manipulate objects.
 */
public class ProbeReflectionUtils {

    /**
     * Test if the given class implements the java.util.Collection interface. The whole hierarchy of the class is
     * tested.
     *
     * @param f The class to be tested.
     * @return True if the class is a Collection, false otherwise.
     */
    public static boolean isCollection(Class<?> f) {
        Class<?>[] intf = f.getInterfaces();
        boolean flag = false;

        for (int i = 0; i < intf.length && !flag; i++) {
            if (intf[i].getName().equals("java.util.Collection")) flag = true;
            else if (intf[i].getInterfaces().length > 0) if (isCollection(intf[i])) flag = true;
        }

        return flag;
    }

    /**
     * Test if the given class is a wrapper for a native type or a string. It could be a Double, Long, Integer, Float,
     * String, Character or Boolean.
     *
     * @param c The class to be tested.
     * @return True if class is of a native type, false in any other case.
     */
    public static boolean isEditable(Class<?> c) {
        if (c.getSuperclass() == null) return false;

        return (
                (c.getSuperclass().getName().equals("java.lang.Number")) ||
                        (c.getName().equals("java.lang.String")) ||
                        (c.getName().equals("java.lang.Character")) ||
                        (c.getName().equals("java.lang.Boolean"))
        );
    }

    /**
     * Test if the given object is a wrapper for a native type or a string. It could be a Byte, Double, Long, Short,
     * Integer, Float, String, Character or Boolean.
     *
     * @param o The object to be tested.
     * @return True if class is of a native type, false in any other case.
     */
    public static boolean isEditable(Object o) {
        return isEditable(o.getClass());
    }

    /**
     * Test if the given method requires parameters of a native type. In this case the method can be executed. A
     * parameter is native type if int, long, ... or its corresponding wrapper class (Integer, Double, Long, ...).
     *
     * @param m The method to be tested.
     * @return True if the method requires only native-type parameters.
     */
    public static boolean isAnExecutableMethod(Method m) {
        for (Class<?> aClass : m.getParameterTypes()) if (!isEditable(aClass) && !aClass.isPrimitive()) return false;
        return true;
    }

    /**
     * Set a given value wrapped by an Object into the wrapper object.
     *
     * @param o   The object to be updated. It must be of a native wrapper class (String, Double, Long, ...).
     * @param val An object whose toString() method return a valid format for the class type of object o.
     */
    public static void setValueToObject(Object o, Object val) {
        try {

            Field f = o.getClass().getDeclaredField("value");
            f.setAccessible(true);

            if (o instanceof String) {
                char[] ch = new char[val.toString().toCharArray().length];
                System.arraycopy(val.toString().toCharArray(), 0, ch, 0, ch.length);
                f.set(o, ch);

                f = o.getClass().getDeclaredField("count");
                f.setAccessible(true);
                f.set(o, ch.length);

                return;
            }
            switch (o) {
                case Integer ignored -> f.setInt(o, Integer.parseInt(val.toString()));
                case Double ignored -> f.setDouble(o, Double.parseDouble(val.toString()));
                case Boolean ignored -> f.setBoolean(o, Boolean.parseBoolean(val.toString()));
                case Byte ignored -> f.setByte(o, Byte.parseByte(val.toString()));
                case Character ignored -> f.setChar(o, val.toString().charAt(0));
                case Float ignored -> f.setFloat(o, Float.parseFloat(val.toString()));
                case Long ignored -> f.setLong(o, Long.parseLong(val.toString()));
                case Short ignored -> f.setShort(o, Short.parseShort(val.toString()));
                case default -> {
                }
            }
        } catch (Exception e) {
            System.out.println("Err:" + e.getMessage());
        }
    }
}
