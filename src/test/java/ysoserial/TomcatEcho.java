package ysoserial;

public class TomcatEcho {
    static {

    }

    public static Object getFV(Object targetObject, String fieldName) throws Exception {
        java.lang.reflect.Field field = null;
        Class clazz = targetObject.getClass();
        while (clazz != Object.class) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (field == null) {
            throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy of " + targetObject.getClass().getName());
        } else {
            field.setAccessible(true);
            return field.get(targetObject);
        }
    }
}
