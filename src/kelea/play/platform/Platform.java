package kelea.play.platform;

import kelea.play.content.ContentType;
import kelea.play.content.Genre;
import kelea.play.content.Promotable;
import kelea.play.content.Content;
import kelea.play.exceptions.ExistingContentException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Platform {
    private final String name;

    // Catalog indexed by normalized title (see key()): finding, adding and removing a movie by title
    // is a direct lookup instead of scanning a list. LinkedHashMap keeps insertion order when listing.
    private final Map<String, Content> catalog = new LinkedHashMap<>();

    // Number of plays per movie. Content is immutable, so the counter can't live inside it: it lives here.
    private final Map<String, Integer> views = new HashMap<>();

    public Platform(String name) {
        this.name = name;
    }

    /** Map key of a title: trimmed and lowercase, so "Thor", " thor " and "THOR" are the same movie. */
    private static String key(String title) {
        return title.trim().toLowerCase();
    }

    /**
     * Adds a movie to the catalog.
     * Two movies are the same when they have the same title, ignoring case.
     *
     * @throws ExistingContentException if a movie with that title is already in the catalog
     */
    public void addContent(Content movie) throws ExistingContentException {
        // putIfAbsent returns the movie already stored under that key, or null if the key was free
        if (catalog.putIfAbsent(key(movie.title()), movie) != null) {
            throw new ExistingContentException(movie.title());
        }
    }

    /**
     * Plays a movie by exact title: counts one more view and prints "playing ...".
     *
     * @return the movie, or empty if there is no movie with that title
     */
    public Optional<Content> play(String title) {
        Content movie = catalog.get(key(title));
        if (movie == null) {
            return Optional.empty();
        }

        // merge: if the key has no value put 1, otherwise combine the old value with 1 using Integer::sum
        views.merge(key(title), 1, Integer::sum);
        movie.play();
        return Optional.of(movie);
    }

    /** Times the movie has been played; getOrDefault returns 0 when it never was. */
    public int viewsOf(Content movie) {
        return views.getOrDefault(key(movie.title()), 0);
    }

    /**
     * Played movies with their views, most viewed first.
     * The result is a LinkedHashMap because a plain HashMap would lose the sorted order.
     */
    public Map<Content, Integer> mostViewed() {
        return views.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        entry -> catalog.get(entry.getKey()),
                        Map.Entry::getValue,
                        (first, second) -> first,   // merge function: required, only used if two keys collide
                        LinkedHashMap::new));
    }

    /**
     * Finds movies whose title contains the given text, ignoring case.
     * Returns a list (possibly empty) because several titles can match a partial search.
     */
    public List<Content> findByTitle(String text) {
        String search = text.trim().toLowerCase();
        return catalog.values().stream()
                .filter(movie -> movie.title().toLowerCase().contains(search))
                .toList();
    }

    /** Finds movies of a genre. Enums are compared with == because each constant exists only once. */
    public List<Content> findByGenre(Genre genre) {
        return catalog.values().stream()
                .filter(movie -> movie.genre() == genre)
                .toList();
    }

    /**
     * Finds contents of a kind (movies or documentaries).
     * type() is abstract in Content: each subclass answers for itself, so this filter never needs to know
     * the subclasses (with instanceof checks it would have to be edited for every new kind).
     */
    public List<Content> findByType(ContentType type) {
        return catalog.values().stream()
                .filter(content -> content.type() == type)
                .toList();
    }

    /**
     * Everything in the catalog that can be promoted, seen as Promotable (not as Content).
     * Content doesn't implement Promotable, only some subclasses do, so we keep the ones that do:
     * isInstance asks "does it implement the interface?" and cast converts it to that type.
     */
    public List<Promotable> promotions() {
        return catalog.values().stream()
                .filter(Promotable.class::isInstance)
                .map(Promotable.class::cast)
                .toList();
    }

    /**
     * Total duration of the whole catalog, in minutes.
     * mapToLong is map for primitives: it turns each Content into its duration (a long), and sum() adds them up.
     */
    public long totalDuration() {
        return catalog.values().stream()
                .mapToLong(Content::duration)
                .sum();
    }

    /**
     * Builds a summary of the catalog.
     * average() returns an OptionalDouble (empty for an empty stream), so orElse(0) avoids dividing by zero.
     */
    public CatalogStats stats() {
        double averageNote = catalog.values().stream()
                .mapToDouble(Content::note)
                .average()
                .orElse(0);

        return new CatalogStats(catalog.size(), totalDuration(), averageNote);
    }

    /**
     * Shortest movie of the catalog.
     * Returns Optional because an empty catalog has no shortest movie: the caller is forced to handle that case
     * instead of receiving null.
     */
    public Optional<Content> shortestMovie() {
        return catalog.values().stream()
                .min(Comparator.comparingLong(Content::duration));
    }

    /** Longest movie of the catalog. Same idea as shortestMovie(), but with max(). */
    public Optional<Content> longestMovie() {
        return catalog.values().stream()
                .max(Comparator.comparingLong(Content::duration));
    }

    /**
     * Shortest and longest movie together; empty if the catalog is empty.
     * flatMap/map on Optional chain "if present" steps: the record is only built when both movies exist.
     */
    public Optional<DurationRange> durationRange() {
        return shortestMovie().flatMap(shortest ->
                longestMovie().map(longest -> new DurationRange(shortest, longest)));
    }

    /**
     * Movies whose note is exactly the given one (e.g. 4.5), sorted alphabetically.
     * Returns a list because several movies can share the same note.
     */
    public List<Content> moviesByNote(double note) {
        return catalog.values().stream()
                // Double.compare is the safe way to compare doubles for equality
                .filter(movie -> Double.compare(movie.note(), note) == 0)
                .sorted(Comparator.comparing(Content::title))
                .toList();
    }

    /**
     * Popular movies (note >= 4), best rated first.
     * Content::isPopular and Content::note are method references: shorthand for movie -> movie.isPopular().
     */
    public List<Content> topRated(long top) {
        return catalog.values().stream()
                .filter(Content::isPopular)
                .sorted(Comparator.comparing(Content::note).reversed())
                .limit(top)
                .toList();
    }

    /**
     * Removes the movie with exactly that title (ignoring case), and its views.
     * Exact match on purpose: unlike searching, deleting by partial text could remove the wrong movie.
     *
     * @return true if a movie was removed
     */
    public boolean removeByTitle(String title) {
        views.remove(key(title));
        // remove returns the removed value, or null if the key didn't exist
        return catalog.remove(key(title)) != null;
    }

    /** Copy of the catalog as a list, so callers can't modify the internal map. */
    public List<Content> getContent() {
        return List.copyOf(catalog.values());
    }

    public String getName() {
        return name;
    }
}
