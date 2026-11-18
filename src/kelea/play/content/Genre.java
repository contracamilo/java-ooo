package kelea.play.content;

/**
 * Fixed set of genres. An enum instead of a String prevents typos ("Sci-fi", "scifi", "Sci Fi")
 * from creating different genres, and the compiler checks every use.
 */
public enum Genre {
    ACTION("Action"),
    ANIMATION("Animation"),
    COMEDY("Comedy"),
    CRIME("Crime"),
    DRAMA("Drama"),
    HORROR("Horror"),
    ROMANCE("Romance"),
    SCI_FI("Sci-Fi");

    // Human-friendly name: the constant names (SCI_FI) are for code, the label is for the user
    private final String label;

    Genre(String label) {
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
