package dev.bug.either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class EitherCollectors<L,R> implements
    Collector< Either<L,R>,
    EitherCollectors.EitherAccumulator<L,R>,
        Either<List<L>, List<R>> > {

    private final boolean leftBiased;

    public static <L,R> Collector<Either<L,R>, ?, Either<List<L>, List<R>>> toLeftBiased() {
        return new EitherCollectors<>(true);
    }

    public static <L,R> Collector<Either<L,R>, ?, Either<List<L>, List<R>>> toRightBiased() {
        return new EitherCollectors<>(false);
    }

    private EitherCollectors(boolean leftBiased) {
        this.leftBiased = leftBiased;
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    public Supplier<EitherAccumulator<L, R>> supplier() {
        return () -> new EitherAccumulator<>(leftBiased);
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    public BiConsumer<EitherAccumulator<L, R>, Either<L, R>> accumulator() {
        return EitherAccumulator::add;
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    public BinaryOperator<EitherAccumulator<L, R>> combiner() {
        return EitherAccumulator::append;
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    public Function<EitherAccumulator<L, R>, Either<List<L>, List<R>>> finisher() {
        return EitherAccumulator::finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Characteristics.CONCURRENT));
    }

    static class EitherAccumulator<L,R> {
        private final List<L> lefts;
        private final List<R> rights;
        private final boolean leftBiased;

        EitherAccumulator(boolean leftBiased) {
            this.leftBiased = leftBiased;
            this.lefts = new ArrayList<>();
            this.rights = new ArrayList<>();
        }

        void add(Either<L,R> e) {
            e.run(lefts::add, rights::add);
        }

        EitherAccumulator<L,R> append(EitherAccumulator<L,R> accumulator2) {
            lefts.addAll(accumulator2.lefts);
            rights.addAll(accumulator2.rights);
            return this;
        }

        Either<List<L>, List<R>> finisher() {
            if(leftBiased) {
                return !lefts.isEmpty() || rights.isEmpty()  ? Either.left(lefts) : Either.right(rights);
            } else {
                return !rights.isEmpty() || lefts.isEmpty() ? Either.right(rights) : Either.left(lefts);
            }
        }
    }

}
