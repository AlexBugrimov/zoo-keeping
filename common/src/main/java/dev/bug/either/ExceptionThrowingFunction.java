package dev.bug.either;

@FunctionalInterface
public interface ExceptionThrowingFunction<T,R> {
    R apply(T t) throws Exception;
}
