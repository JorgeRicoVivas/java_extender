package org.jorge_rico_vivas.java_extender.string;

import org.jorge_rico_vivas.java_extender.option.Option;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class StringFormat {

/*
    The literal pattern is:

#removes trailing spaces and commas to the left
[\s,]*
(?<fullGroup>
#Represents a group with qualifications, in this case the unformatted string
#doesn't get replaced with the string written on 'replacements', instead, that
#string is matched against a set of conditions called 'qualifications'
#Example: Books: {0|M: books, 1: book}, in this case, the argument should be
#a number, such as Integers, Longs...

((?<qualificatedGroup>[^,]*)\s*(->|:)\s*)?(\{(?<qualifications>.*?)\})
|
#Represents a simple group where the unformatted string will be replaced by:
#-if the content is an index: The string with that index on replacements
#-if there is content: The string whose index corresponds to the name with
#      that content
#-if the content is empty: Autoresolved by calculating a proper index
(?<simpleGroup>[^,]*)
)
#removes trailing spaces and commas to the right
[\s,]*
 */

    public static final Pattern GROUP_PATTERN = Pattern.compile(
            "#removes trailing spaces and commas to the left\n" +
                    "[\\s,]*\n" +
                    "(?<fullGroup>\n" +
                    "#Represents a group with qualifications, in this case the unformatted string\n" +
                    "#doesn't get replaced with the string written on 'replacements', instead, that\n" +
                    "#string is matched against a set of conditions called 'qualifications'\n" +
                    "#Example: Books: {0|M: books, 1: book}, in this case, the argument should be\n" +
                    "#a number, such as Integers, Longs...\n" +
                    "\n" +
                    "((?<qualificatedGroup>[^,]*)\\s*(->|:)\\s*)?(\\{(?<qualifications>.*?)\\})\n" +
                    "|\n" +
                    "#Represents a simple group where the unformatted string will be replaced by:\n" +
                    "#-if the content is an index: The string with that index on replacements\n" +
                    "#-if there is content: The string whose index corresponds to the name with\n" +
                    "#      that content\n" +
                    "#-if the content is empty: Autoresolved by calculating a proper index\n" +
                    "(?<simpleGroup>[^,]*)\n" +
                    ")\n" +
                    "#removes trailing spaces and commas to the right\n" +
                    "[\\s,]*"
            , Pattern.COMMENTS);

    public static final Pattern QUALIFIERS_PRE_INDICATED_PATTERN = Pattern.compile("((?<resolver>[^,]*)\\s*(->|:)\\s*)?(\\{(?<qualifiersAndReplacements>.*?)\\})");

    public static final Pattern QUALIFIERS_INLINED_PATTERN = Pattern.compile("^(?<metaResolver>meta((\\s*[:.]\\s*)|\\s+))?(?<resolver>[^,>{-]*)?(->)?(\\{?(?<qualifiersAndReplacements>.*?)\\}?)$");
    public static final Pattern QUALIFIERS_GROUP_PATTERN = Pattern.compile("\\s*(?<qualifiers>.*?)\\s*:\\s?(?<replacement>.*?)\\s?(,|$)");
    public static final Pattern ENCLOSED_REPLACEMENT = Pattern.compile("\\{(.*?)\\}");
    public static final Pattern PRE_INDICATED_NAMED_REPLACEMENTS = Pattern.compile("^\\s*\\[(?<namedGroups>.*)\\]\\s?(?<realSentence>.*)", Pattern.DOTALL);
    public static final Pattern POST_INDICATED_NAMED_REPLACEMENTS = Pattern.compile("(?<realSentence>.*)\\s?\\[(?<namedGroups>.*)\\]\\s*$", Pattern.DOTALL);

    public static String apply(final StringFormatMetadata userMetadata, final String unformattedString, final Object... replacements) {
        Optional<StringFormatMetadata> metadata = Option.of(userMetadata);
        final AtomicReference<String> unformattedStringRef = new AtomicReference<>(unformattedString);
        final AtomicReference<HashMap<String, String>> nameToValue = new AtomicReference<>(new HashMap<>());

        Optional<Matcher> preNamedMatcher = Stream.of(PRE_INDICATED_NAMED_REPLACEMENTS, POST_INDICATED_NAMED_REPLACEMENTS)
                .map(namedReplacementPattern -> namedReplacementPattern.matcher(unformattedStringRef.get()))
                .filter(Matcher::find)
                .findFirst();
        if (preNamedMatcher.isPresent()) {
            unformattedStringRef.set(preNamedMatcher.get().group("realSentence"));
            final AtomicInteger namedIndex = new AtomicInteger();
            String namedGroups = preNamedMatcher.get().group("namedGroups");
            var groupMatcher = GROUP_PATTERN.matcher(namedGroups);
            while (groupMatcher.find()) {
                String groupName;
                int currentReplacementIndex = namedIndex.getAndIncrement();
                if (groupMatcher.group("simpleGroup") != null) {
                    if (groupMatcher.group("simpleGroup").isBlank()) {
                        continue;
                    }
                    groupName = groupMatcher.group("simpleGroup").trim();
                } else {
                    var qualifiersMatcher = QUALIFIERS_PRE_INDICATED_PATTERN.matcher(groupMatcher.group("fullGroup").trim());
                    qualifiersMatcher.find();
                    groupName = qualifiersMatcher.group("resolver").trim();
                    replacements[currentReplacementIndex] = resolveByQualifier(replacements[currentReplacementIndex].toString(), qualifiersMatcher.group("qualifiersAndReplacements").trim());
                }
                nameToValue.get().put(groupName, replacements[currentReplacementIndex].toString());
                unformattedStringRef.set(unformattedStringRef.get().replaceAll("\\{" + groupName + "\\}", replacements[currentReplacementIndex].toString()));
            }

        }

        final AtomicInteger lastPosition = new AtomicInteger(0);
        Function<MatchResult, String> groupToText = matchResult -> {
            String insertionText = matchResult.group(1).trim();
            Optional<String> qualifiersAndReplacements = Optional.empty();
            Matcher qualifiersMatcher = QUALIFIERS_INLINED_PATTERN.matcher(insertionText);
            Optional<String> replacementByMetadata = Optional.empty();
            if (qualifiersMatcher.find()) {
                qualifiersAndReplacements = Option.of(qualifiersMatcher.group("qualifiersAndReplacements").trim());
                if (qualifiersAndReplacements.isEmpty() || qualifiersAndReplacements.get().isBlank()) {
                    qualifiersAndReplacements = Optional.empty();
                }
                insertionText = Option.of(qualifiersMatcher.group("resolver")).orElse("").trim();
                if (qualifiersMatcher.group("metaResolver") != null) {
                    replacementByMetadata = Option.of(metadata.isPresent() ? metadata.get().get(insertionText) : null);
                }
            }
            String replacement = null;
            if (replacementByMetadata.isPresent()) {
                replacement = replacementByMetadata.get();
            } else {
                switch (InsertionKind.of(insertionText)) {
                    case InPlace:
                        replacement = replacements[lastPosition.getAndIncrement()].toString();
                        break;
                    case Indexed:
                        int index = Integer.parseInt(insertionText) - 1;
                        var prev_res = replacements[index];
                        if (lastPosition.get() <= index) {
                            lastPosition.incrementAndGet();
                        }
                        replacement = prev_res.toString();
                        break;
                    case Named:
                        if (!nameToValue.get().containsKey(insertionText)) {
                            nameToValue.get().put(insertionText, replacements[lastPosition.getAndIncrement()].toString());
                        }
                        replacement = nameToValue.get().get(insertionText);
                        break;
                }
            }
            if (qualifiersAndReplacements.isPresent()) {
                return StringFormat.resolveByQualifier(replacement, qualifiersAndReplacements.get());
            }
            return replacement;
        };
        Matcher matcher;
        while ((matcher = ENCLOSED_REPLACEMENT.matcher(unformattedStringRef.get())).find()) {
            unformattedStringRef.set(unformattedStringRef.get().substring(0, matcher.start()) + (groupToText.apply(matcher)) + unformattedStringRef.get().substring(matcher.end()));
        }
        return unformattedStringRef.get();
    }

    private static String resolveByQualifier(String comparingString, String qualifiersAndReplacements) {
        var qualifiersGroupsMatcher = QUALIFIERS_GROUP_PATTERN.matcher(qualifiersAndReplacements);
        while (qualifiersGroupsMatcher.find()) {
            boolean aQualifierMatched = Arrays.stream(qualifiersGroupsMatcher.group("qualifiers").split("\\|"))
                    .map(Qualitification::getFromPattern)
                    .anyMatch(qualifier -> qualifier.isMet(comparingString));
            if (aQualifierMatched) {
                return qualifiersGroupsMatcher.group("replacement");
            }
        }
        return "UNKNOWN";
    }


}