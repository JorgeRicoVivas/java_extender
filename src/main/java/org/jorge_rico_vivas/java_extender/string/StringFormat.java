package org.jorge_rico_vivas.java_extender.string;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class StringFormat {

    public static final Pattern ENCLOSED_REPLACEMENT = Pattern.compile("\\{(.*?)\\}");
    public static final Pattern PRE_INDICATED_NAMED_REPLACEMENTS = Pattern.compile("^\\s*\\[(?<namedGroups>.*)\\]\\s?(?<realSentence>.*)");
    public static final Pattern POST_INDICATED_NAMED_REPLACEMENTS = Pattern.compile("(?<realSentence>.*)\\s?\\[(?<namedGroups>.*)\\]\\s*$");

    public static String apply(String unformattedString, String... replacements) {
        String copyOfOriginalString = unformattedString;
        Optional<Matcher> preNamedMatcher = Stream.of(PRE_INDICATED_NAMED_REPLACEMENTS, POST_INDICATED_NAMED_REPLACEMENTS)
                .map(namedReplacementPattern -> namedReplacementPattern.matcher(copyOfOriginalString))
                .filter(Matcher::find)
                .findFirst();
        if (preNamedMatcher.isPresent()){
            AtomicReference<String> realString = new AtomicReference<>(preNamedMatcher.get().group("realSentence"));
            final AtomicInteger namedIndex = new AtomicInteger();
            Arrays.stream(preNamedMatcher.get().group("namedGroups").split(",")).forEach(name -> {
                realString.set(realString.get().replaceAll("\\{" + name.trim() + "\\}", replacements[namedIndex.getAndIncrement()]));
            });
            unformattedString = realString.get();
        }

        final AtomicInteger lastPosition = new AtomicInteger(0);
        final AtomicReference<HashMap<String, String>> nameToValue = new AtomicReference<>(null);
        String finalUnformattedString = unformattedString;
        return ENCLOSED_REPLACEMENT.matcher(unformattedString)
                .replaceAll(matchResult -> {
                    String insertionText = matchResult.group(1).trim();
                    return switch (InsertionKind.of(insertionText)) {
                        case InPlace -> replacements[lastPosition.getAndIncrement()];
                        case Indexed -> {
                            int index = Integer.parseInt(insertionText) - 1;
                            var prev_res = replacements[index];
                            if (lastPosition.get() <= index) {
                                lastPosition.incrementAndGet();
                            }
                            yield prev_res;
                        }
                        case Named -> {
                            if (nameToValue.get() == null) {
                                var replacement_names = ENCLOSED_REPLACEMENT.matcher(finalUnformattedString)
                                        .results()
                                        .map(matcher -> matcher.group(1))
                                        .distinct()
                                        .filter(enclosed_name -> InsertionKind.Named.equals(InsertionKind.of(enclosed_name)))
                                        .count();
                                nameToValue.set(new HashMap<>((int) replacement_names));
                            }
                            if (!nameToValue.get().containsKey(insertionText)) {
                                nameToValue.get().put(insertionText, replacements[lastPosition.getAndIncrement()]);
                            }
                            yield nameToValue.get().get(insertionText);
                        }
                    };
                });
    }

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
}