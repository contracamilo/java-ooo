package kelea.play.content;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A movie: a Content that also has a director. "final": it can't have subclasses.
 * extends gives it what it IS (a Content); implements gives it what it CAN DO (be promoted). A class can only
 * extend one class, but implement as many interfaces as it wants.
 */
public final class Movie extends Content implements Promotable {

    /** Used for movies loaded from old files, written before directors existed. */
    public static final String UNKNOWN_DIRECTOR = "Unknown";

    private final String director;

    public Movie(String title, String desc, Genre genre, Language language, Quality quality,
                 LocalDate releaseDate, double note, boolean available, long duration, String director) {
        super(title, desc, genre, language, quality, releaseDate, note, available, duration);
        this.director = Objects.requireNonNull(director, "director is required");
        if (director.isBlank()) {
            throw new IllegalArgumentException("director can't be blank");
        }
    }

    public Movie(String title, Genre genre, Language language, Quality quality, long duration, double note,
                 String director) {
        this(title, null, genre, language, quality, null, note, false, duration, director);
    }

    /** Short constructor when the director is not known. */
    public Movie(String title, Genre genre, Language language, Quality quality, long duration, double note) {
        this(title, genre, language, quality, duration, note, UNKNOWN_DIRECTOR);
    }

    public String director() {
        return director;
    }

    @Override
    public ContentType type() {
        return ContentType.MOVIE;
    }

    @Override
    public void play() {
        System.out.println("playing " + title());
    }

    @Override
    public String promoText() {
        String by = director.equals(UNKNOWN_DIRECTOR) ? "" : ", directed by " + director;
        return "Don't miss " + title() + by + " (" + note() + "/5)";
    }

    @Override
    public String details() {
        return "Director: " + director;
    }

    @Override
    public Movie withAvailable(boolean available) {
        return new Movie(title(), desc(), genre(), language(), quality(), releaseDate(), note(), available,
                duration(), director);
    }
}
