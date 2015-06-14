package org.jsonbuddy;

public class ExceptionUtil {
    public static RuntimeException soften(Exception e) {
        return softenHelper(e);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Exception> T softenHelper(Exception e) throws T {
        throw (T)e;
    }
}