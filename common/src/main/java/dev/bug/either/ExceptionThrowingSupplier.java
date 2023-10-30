package dev.bug.either;

@FunctionalInterface
public interface ExceptionThrowingSupplier<T> {
    T get() throws Exception;
}