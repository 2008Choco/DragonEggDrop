package com.ninjaguild.dragoneggdrop.utils;

import java.util.function.Function;

public final class ObjectResultWrapper<T> {

    private final T result;
    private final String reason;
    private final boolean success;
    private final Throwable throwable;

    private ObjectResultWrapper(T result, String reason, boolean success, Throwable throwable) {
        this.result = result;
        this.reason = reason;
        this.success = success;
        this.throwable = throwable;
    }

    public T getResult() {
        return result;
    }

    public String getReason() {
        return reason;
    }

    public boolean hasReason() {
        return reason != null;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !isSuccess();
    }

    public boolean failedExceptionally() {
        return throwable != null;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public static <T> ObjectResultWrapper<T> success(T result) {
        return new ObjectResultWrapper<>(result, "", true, null);
    }

    public static <T> ObjectResultWrapper<T> fail(String reason) {
        return new ObjectResultWrapper<>(null, reason, false, null);
    }

    public static <T> ObjectResultWrapper<T> failExceptionally(String reason, Throwable throwable) {
        return new ObjectResultWrapper<>(null, reason, false, throwable);
    }

    public static <T> ObjectResultWrapper<T> failExceptionally(String reason, Function<String, Throwable> throwable) {
        return new ObjectResultWrapper<>(null, reason, false, throwable.apply(reason));
    }

}
