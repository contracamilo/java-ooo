package kelea.play.content;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Anything the platform can play. Holds what every kind of content has in common; each subclass adds what
 * is specific to it (INHERITANCE: Movie and Documentary "are a" Content).
 *
 * - abstract: it can't be created with new Content(...); only subclasses can exist. It is a concept, not a thing.
 * - sealed ... permits: ONLY Movie and Documentary may extend it. The compiler knows every possible subclass,
 *   so a switch over a Content doesn't need a "default" branch.
 *
 * Content stays immutable like the old Movie record was: all fields are final and there are no setters.
 * (A record can't extend a class, which is why this is a normal class now.)
 */
public abstract sealed class Content permits Movie, Documentary {

    private final String title;
    private final String desc;
    private final Genre genre;
    private final Language language;
    private final Quality quality;
    private final LocalDate releaseDate;
    private final double note;
    private final boolean available;
    private final long duration;

    // protected: only subclasses can call it, with super(...) from their own constructors
    protected Content(String title, String desc, Genre genre, Language language, Quality quality,
                      LocalDate releaseDate, double note, boolean available, long duration) {
        this.title = Objects.requireNonNull(title, "title is required");
        this.desc = desc;
        this.genre = Objects.requireNonNull(genre, "genre is required");
        this.language = Objects.requireNonNull(language, "language is required");
        this.quality = Objects.requireNonNull(quality, "quality is required");
        this.releaseDate = releaseDate;
        if (note < 0 || note > 5) {
            throw new IllegalArgumentException("note must be between 0 and 5: " + note);
        }
        this.note = note;
        this.available = available;
        this.duration = duration;
    }

    // Abstract methods have no body: every concrete subclass MUST implement them (POLYMORPHISM: the same
    // call, content.play(), does something different depending on the real type of the object).
    public abstract ContentType type();

    public abstract void play();

    /** What is specific to each kind, as text (director, topic...). */
    public abstract String details();

    /** Copy with another availability. Each subclass returns its own type (covariant return type). */
    public abstract Content withAvailable(boolean available);

    public boolean isPopular() {
        return note >= 4;
    }

    // Accessors keep the record style (title(), not getTitle()) so the rest of the code didn't change
    public String title() {
        return title;
    }

    public String desc() {
        return desc;
    }

    public Genre genre() {
        return genre;
    }

    public Language language() {
        return language;
    }

    public Quality quality() {
        return quality;
    }

    public LocalDate releaseDate() {
        return releaseDate;
    }

    public double note() {
        return note;
    }

    public boolean available() {
        return available;
    }

    public long duration() {
        return duration;
    }

    // A record generated equals/hashCode for us; a class gets identity comparison unless we write them.
    // Same title (ignoring case) = same content, which is also how the catalog decides what is a duplicate.
    @Override
    public boolean equals(Object other) {
        return other instanceof Content content
                && title.equalsIgnoreCase(content.title);
    }

    @Override
    public int hashCode() {
        // Must be consistent with equals: titles that are equal ignoring case must give the same hash
        return title.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return type() + " " + title;
    }
}
