package org.jorge_rico_vivas.java_extender.option;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Option {

    public static <T> Optional<T> of(T... elements) {
        if (elements == null || elements.length == 0) {
            return Optional.empty();
        }
        switch (elements.length) {
            case 1:
                return elements[0] != null ? Optional.of(elements[0]) : Optional.empty();
            case 2:
                return elements[0] != null ? Optional.of(elements[0]) :
                        elements[1] != null ? Optional.of(elements[1]) : Optional.empty();
            default:
                return Stream.of(elements).filter(Objects::nonNull).findFirst();
        }
    }

}
