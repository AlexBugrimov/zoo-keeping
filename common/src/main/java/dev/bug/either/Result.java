package dev.bug.either;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Result<R> {

    public static <R> Result<R> attempt(ExceptionThrowingSupplier<R> resultSupplier){
        try {
            R resultValue = resultSupplier.get();
            return Result.ok(resultValue);
        } catch (Exception e){
            return Result.err(e);
        }
    }

    public static <R> Result<R> err(Exception e){ return new Err<>(e); }

    public static <R> Result<R> ok(R result){ return new Ok<>(result); }

    public abstract Exception getException();

    public abstract R getResult();

    public R getOrElse(R other) {
        return fold(
            exception -> other,
            Function.identity()
        );
    }

    public R getOrElse(Supplier<R> otherSupplier) {
        return fold(
            exception -> otherSupplier.get(),
            Function.identity()
        );
    }

    public Optional<R> toOptional() {
        return fold(
            exception -> Optional.empty(),
            Optional::ofNullable
        );
    }

    public abstract boolean isErr();
    public abstract boolean isOk();

    public abstract <T> T fold(Function<Exception,T> transformException, Function<R,T> transformValue);

    public abstract <T> Result<T> map(ExceptionThrowingFunction<R,T> transformValue);

    public abstract <T> Result<T> flatMap(ExceptionThrowingFunction<R, Result<T>> transformValue);

    public abstract <X extends Throwable> R getOrElseThrow(Supplier<X> exceptionSupplier) throws X;

    public abstract void ifOk(Consumer<R> acceptsOkValue);

    public abstract void run(Consumer<Exception> errorHandler, Consumer<R> okHandler);

    public static class Err<R> extends Result<R> {
        private final Exception ex;
        private Err(Exception e) {
            this.ex = e;
        }

        @Override
        public Exception getException() { return this.ex; }
        @Override
        public R getResult() { throw new NoSuchElementException("Tried to getResult from an Err"); }

        @Override
        public boolean isErr() { return true; }
        @Override
        public boolean isOk() { return false; }

        @Override
        public <T> T fold(Function<Exception, T> transformException, Function<R, T> transformValue) {
            return transformException.apply(this.ex);
        }

        @Override
        public <T> Result<T> map(ExceptionThrowingFunction<R, T> transformRight) {
            return Result.<T>err(this.ex);
        }
        @Override
        public <T> Result<T> flatMap(ExceptionThrowingFunction<R, Result<T>> transformValue) {
            return Result.<T>err(this.ex);
        }

        @Override
        public <X extends Throwable> R getOrElseThrow(Supplier<X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }

        @Override
        public void ifOk(Consumer<R> acceptsOkValue) { /* no-op */ }
        @Override
        public void run(Consumer<Exception> errorHandler, Consumer<R> okHandler) {
            errorHandler.accept(this.ex);
        }

        @Override
        public int hashCode(){ return this.ex.hashCode(); }

        @Override
        public boolean equals(Object other){
            if (other instanceof Err<?> otherAsErr){
                return this.ex.equals(otherAsErr.ex);
            } else {
                return false;
            }
        }

    }
    public static class Ok<R> extends Result<R> {

        private final R resultValue;
        private Ok(R value) {
            this.resultValue = value;
        }

        @Override
        public Exception getException() { throw new NoSuchElementException("Tried to getException from an Ok"); }
        @Override
        public R getResult() { return resultValue; }

        @Override
        public boolean isErr() { return false; }
        @Override
        public boolean isOk() { return true; }

        @Override
        public <T> T fold(Function<Exception, T> transformException, Function<R, T> transformValue) {
            return transformValue.apply(this.resultValue);
        }
        @Override
        public <T> Result<T> map(ExceptionThrowingFunction<R, T> transformValue) {
            return Result.attempt(() -> transformValue.apply(this.resultValue));
        }
        @Override
        public <T> Result<T> flatMap(ExceptionThrowingFunction<R, Result<T>> transformValue) {
            try {
                return transformValue.apply(this.resultValue);
            } catch(Exception e) {
                return new Err<T>(e);
            }
        }

        @Override
        public <X extends Throwable> R getOrElseThrow(Supplier<X> exceptionSupplier) throws X {
            return resultValue;
        }

        @Override
        public void ifOk(Consumer<R> acceptsOkValue) {
            acceptsOkValue.accept(this.resultValue);
        }
        @Override
        public void run(Consumer<Exception> errorHandler, Consumer<R> okHandler) {
            okHandler.accept(this.resultValue);
        }

        @Override
        public int hashCode(){ return this.resultValue.hashCode(); }

        @Override
        public boolean equals(Object other){
            if (other instanceof Ok<?> otherAsOk){
                return this.resultValue.equals(otherAsOk.resultValue);
            } else {
                return false;
            }
        }
    }
}
