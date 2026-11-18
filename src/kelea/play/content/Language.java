package kelea.play.content;

/** Audio language of a movie. Enum for the same reason as Genre: a closed set of valid values. */
public enum Language {
    ENGLISH("English"),
    SPANISH("Spanish"),
    FRENCH("French"),
    GERMAN("German"),
    ITALIAN("Italian"),
    JAPANESE("Japanese"),
    PORTUGUESE("Portuguese");

    private final String label;

    Language(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
