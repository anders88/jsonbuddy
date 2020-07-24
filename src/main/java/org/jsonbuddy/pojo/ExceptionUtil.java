package org.jsonbuddy.pojo;

import java.util.function.Function;

class ExceptionUtil {

    /**
     * Throws a checked exception as an unchecked exception without the compiler noticing.
     * This is possible because of generics type erasure and the fact that exception checking
     * happens only at runtime.
     */
    static RuntimeException soften(Exception e) {
        return softenHelper(e);
    }

    @FunctionalInterface
    public interface FunctionWithException<T, R, EX extends Exception> {
        R apply(T o) throws EX;
    }

    static <T, R, EX extends Exception> Function<T, R> softenFunction(FunctionWithException<T, R, EX> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Exception e) {
                throw softenHelper(e);
            }
        };
    }


    @SuppressWarnings("unchecked")
    private static <T extends Exception> T softenHelper(Exception e) throws T {
        throw (T)e;
    }
}