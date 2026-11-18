package kelea.play.content;

/**
 * Video quality, ordered from lowest to highest: declaration order gives compareTo()/ordinal() a meaning,
 * so e.g. quality.compareTo(Quality.HD) >= 0 reads "HD or better".
 * UHD_4K can't be named "4K" because Java identifiers can't start with a digit.
 */
public enum Quality {
    SD("SD"),
    HD("HD"),
    FULL_HD("Full HD"),
    UHD_4K("4K");

    private final String label;

    Quality(String label) {
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
