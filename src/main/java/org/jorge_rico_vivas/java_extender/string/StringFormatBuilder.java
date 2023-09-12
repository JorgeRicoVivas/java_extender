package org.jorge_rico_vivas.java_extender.string;

class StringFormatBuilder {
    StringFormatMetadata metadata;
    String unformattedString;
    Object[] replacements;

    public StringFormatBuilder(String unformattedString, Object[] replacements) {
        this.unformattedString = unformattedString;
        this.replacements = replacements;
    }

    public StringFormatBuilder() {
    }

    public void metadata(StringFormatMetadata metadata) {
        this.metadata = metadata;
    }

    public void unformattedString(String unformattedString) {
        this.unformattedString = unformattedString;
    }

    public void replacements(Object[] replacements) {
        this.replacements = replacements;
    }

    public String resolve() {
        return StringFormat.apply(metadata, unformattedString, replacements);
    }
}
