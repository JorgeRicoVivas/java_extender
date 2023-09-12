package org.jorge_rico_vivas.java_extender.string;

import org.jorge_rico_vivas.java_extender.function.Comparators;
import org.jorge_rico_vivas.java_extender.regexs.Regexs;

import java.util.Arrays;
import java.util.regex.Pattern;

interface Qualitification {
    static Qualitification getFromPattern(String qualificator) {
        var qualificationComparisonMatcher = ComparingQualification.QUALIFICATION_COMPARISON_PATTERN.matcher(qualificator);
        if (qualificationComparisonMatcher.find()) {
            var comparator = Comparators.fromString(qualificationComparisonMatcher.group("comparator"));
            return new ComparingQualification(comparator, Float.parseFloat(qualificationComparisonMatcher.group("comparingNumber")));
        }
        if (Arrays.asList("N", "M").contains(qualificator.trim())) {
            return new ComparingQualification(Comparators.GREATER, 1);
        }
        if (qualificator.isBlank() || qualificator.trim().equalsIgnoreCase("default")) {
            return new AlwaysTrueQualitification();
        }
        return new ComparingString(qualificator);
    }

    boolean isMet(String introducedString);

    final class ComparingQualification implements Qualitification {
        public static final Pattern QUALIFICATION_COMPARISON_PATTERN = Pattern.compile("\\s*(N\\s*)?(?<comparator>>=|<=|!=|>|<|==)\\s*(?<comparingNumber>" + Regexs.NUMBER_REGEX.pattern() + ")\\s*");
        private final Comparators comparator;
        private final Number comparingNumber;

        public ComparingQualification(Comparators comparator, Number comparingNumber) {
            this.comparator = comparator;
            this.comparingNumber = comparingNumber;
        }

        @Override
        public boolean isMet(String introducedString) {
            try {
                var introducedFloat = Float.valueOf(introducedString);
                return comparator.<Float>comparator().test(introducedFloat, comparingNumber.floatValue());
            } catch (Exception ex) {
                return false;
            }
        }
    }

    final class ComparingString implements Qualitification {
        private final String comparingString;

        public ComparingString(String comparingString) {
            this.comparingString = comparingString;
        }

        @Override
        public boolean isMet(String introducedString) {
            return comparingString.equals(introducedString);
        }
    }

    final class AlwaysTrueQualitification implements Qualitification {

        @Override
        public boolean isMet(String introducedString) {
            return true;
        }
    }
}
