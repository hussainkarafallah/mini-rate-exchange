package com.hussainkarafallah.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.Callable;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionUtils {

    public static <T> T doUnchecked(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable t) {
            throw toUnchecked(t);
        }
    }

    public static void doUnchecked(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            throw toUnchecked(t);
        }
    }

    public static RuntimeException toUnchecked(Throwable t) {
        if (t instanceof Error) {
            throw (Error) t;
        }
        if (t instanceof UndeclaredThrowableException) {
            Throwable cause = t.getCause();
            if (cause != null) {
                throw ExceptionUtils.toUnchecked(cause);
            }
        }
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        if (t instanceof InvocationTargetException) {
            Throwable cause = t.getCause();
            if (cause != null) {
                throw ExceptionUtils.toUnchecked(cause);
            }
        }
        throw new RuntimeException(t);
    }

    public static String rootCauseToString(Throwable t, @SuppressWarnings("SameParameterValue") int stacksCount) {
        if (t == null) {
            return "";
        }
        Throwable root = t;
        while (t != null) {
            root = t;
            t = t.getCause();
        }

        return ExceptionUtils.toString(root, stacksCount);
    }

    public static String toString(Throwable t, int stacksCount) {
        StringBuilder sb = new StringBuilder();
        while (t != null) {
            sb.append(t.toString());
            if (stacksCount > 0) {
                StackTraceElement[] stackTraceElement = t.getStackTrace();
                int count = Math.min(stacksCount, stackTraceElement.length);
                for (int i = 0; i < count; i++) {
                    sb.append("\n");
                    sb.append(stackTraceElement[i].toString());
                }
                if (stackTraceElement.length > count) {
                    sb.append("\n...");
                }
            }
            t = t.getCause();
            if (t != null) {
                sb.append("\nCaused By: ");
            }
        }
        return sb.toString();
    }

    public static String getAllCauseMessages(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getMessage());
        while ((t = t.getCause()) != null) {
            sb.append("\n").append(t.getMessage());
        }
        return sb.toString();
    }
}