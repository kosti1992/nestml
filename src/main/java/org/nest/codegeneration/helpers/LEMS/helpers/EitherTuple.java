package org.nest.codegeneration.helpers.LEMS.helpers;

import java.util.Optional;

/**
 * This is an adjusted version of the Either class.
 * @author kperun
 */
public class EitherTuple<A, B> {
    private Optional<A> left;
    private Optional<B> right;

    private EitherTuple(Optional<A> _left, Optional<B> _right) {
        this.left = _left;
        this.right = _right;
    }

    public static <TypeA, TypeB> EitherTuple<TypeA, TypeB> newLeft(final TypeA left) {
        return new EitherTuple<>(Optional.of(left), Optional.empty());
    }

    public static <LType, RType> EitherTuple<LType, RType> newRight(final RType right) {
        return new EitherTuple<>(Optional.empty(), Optional.of(right));
    }

    public A getLeft() {
        return left.get();
    }

    public B getRight() {
        return right.get();
    }

    public boolean isLeft() {
        return left.isPresent();
    }

    public boolean isRight() {
        return right.isPresent();
    }

    public String toString() {
        return "(" + left + ", " + right + ")";
    }


}