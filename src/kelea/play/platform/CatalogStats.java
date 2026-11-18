package kelea.play.platform;

/**
 * Summary of the catalog at a given moment. Being a record, it is a snapshot: if the catalog
 * changes later, ask Platform.stats() for a new one.
 */
public record CatalogStats(
        long movieCount,
        long totalMinutes,
        double averageNote
){

    // Compact constructor: validates before the fields are assigned
    public CatalogStats {
        if (movieCount < 0 || totalMinutes < 0) {
            throw new IllegalArgumentException("counts can't be negative");
        }
    }

    /** Derived value (not a component, so it isn't stored): average length of a movie. */
    public double averageMinutes() {
        return movieCount == 0 ? 0 : (double) totalMinutes / movieCount;
    }
}
