package kelea.play.platform;

import kelea.play.content.Content;

import java.util.List;
import java.util.Objects;

/**
 * Shortest and longest movie of the catalog, as one value.
 *
 * A record is a class for immutable data: the compiler generates the constructor, the getters
 * (shortest(), longest(), without "get"), equals(), hashCode() and toString() from the components below.
 */
public record DurationRange(Content shortest, Content longest) {

    // Compact constructor: no parameter list, runs before the fields are assigned. Used to validate.
    public DurationRange {
        Objects.requireNonNull(shortest, "shortest is required");
        Objects.requireNonNull(longest, "longest is required");
        if (shortest.duration() > longest.duration()) {
            throw new IllegalArgumentException("shortest can't be longer than longest");
        }
    }

    /** Records can have normal methods: this one is derived data, so it is not stored as a component. */
    public long differenceInMinutes() {
        return longest.duration() - shortest.duration();
    }

    public List<Content> asList() {
        return List.of(shortest, longest);
    }
}
