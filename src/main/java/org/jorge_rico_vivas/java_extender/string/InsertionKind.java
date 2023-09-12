package org.jorge_rico_vivas.java_extender.string;

import java.util.stream.Stream;

enum InsertionKind {
    InPlace, Indexed, Named;

    public static InsertionKind of(String InsertionText) {
        if (InsertionText.isEmpty()) {
            return InPlace;
        } else if (Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(Object::toString).anyMatch(InsertionText::startsWith)) {
            return Indexed;
        } else {
            return Named;
        }
    }
}
