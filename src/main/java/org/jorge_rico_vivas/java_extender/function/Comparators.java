package org.jorge_rico_vivas.java_extender.function;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

public enum Comparators {
    EQUALS, NOT_EQUALS, GREATER, GREATER_OR_EQUALS, LESS, LESS_OR_EQUALS;
    public static final List<Comparators> VARIANTS = Arrays.asList(EQUALS, NOT_EQUALS, GREATER, GREATER_OR_EQUALS, LESS, LESS_OR_EQUALS);
    public static final List<String> STRINGS = Arrays.asList("==", "!=", ">", ">=", "<", "<=");

    public static Comparators fromString(String string) {
        int index = STRINGS.indexOf(string);
        return index >= 0 ? VARIANTS.get(index) : null;
    }

    public <T extends Comparable<T>> BiPredicate<T, T> comparator() {
        return (a, b) -> {
            int comparingResult = a.compareTo(b);
            switch (this) {
                case EQUALS:
                    return comparingResult == 0;
                case NOT_EQUALS:
                    return comparingResult != 0;
                case GREATER:
                    return comparingResult > 0;
                case GREATER_OR_EQUALS:
                    return comparingResult >= 0;
                case LESS:
                    return comparingResult < 0;
                case LESS_OR_EQUALS:
                    return comparingResult <= 0;
                default:
                    return false;
            }
        };
    }

    public <T extends Comparable<T>> boolean isMetBy(T element, T comparedElement) {
        return this.<T>comparator().test(element, comparedElement);
    }
}
