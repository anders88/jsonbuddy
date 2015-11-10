package org.jsonbuddy.pojo;

class ExceptionUtil {

    /**
     * Throws a checked exception as an unchecked exception without the compiler noticing.
     * This is possible because of generics type erasure and the fact that exception checking
     * happens only at runtime.
     */
    static RuntimeException soften(Exception e) {
        return softenHelper(e);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Exception> T softenHelper(Exception e) throws T {
        throw (T)e;
    }
}