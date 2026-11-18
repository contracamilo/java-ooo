package kelea.play.content;

/** Kind of content in the catalog. Used in the file format and when asking the user what to add. */
public enum ContentType {
    MOVIE("Movie"),
    DOCUMENTARY("Documentary");

    private final String label;

    ContentType(String label) {
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
