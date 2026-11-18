package kelea.play;

import kelea.play.content.Content;
import kelea.play.content.ContentType;
import kelea.play.content.Documentary;
import kelea.play.content.Genre;
import kelea.play.content.Language;
import kelea.play.content.Movie;
import kelea.play.content.Promotable;
import kelea.play.content.Quality;
import kelea.play.exceptions.ExistingContentException;
import kelea.play.platform.CatalogStats;
import kelea.play.platform.DurationRange;
import kelea.play.platform.Platform;
import kelea.play.repository.ContentRepository;
import kelea.play.repository.FileContentRepository;
import kelea.play.util.ConsoleUtils;
import kelea.play.util.ScannerUtils;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class Main {
    public static final String PLATFORM_NAME = "KELEA PLAY";
    public static final String VERSION = "1.0.0";
    public static boolean running = true;

    // Where the catalog lives. Path.of builds the path with the OS separator; it is relative to where the app is run
    private static final Path CATALOG_FILE = Path.of("data", "movies.csv");

    // Main only knows the interface: changing where the catalog is stored means changing only this line
    private static final ContentRepository REPOSITORY = new FileContentRepository(CATALOG_FILE);

    // false when the load failed for an unknown reason: saving then could overwrite a file we couldn't read
    private static boolean canSave = true;

    public static void main(String[] args) {
        System.out.println(PLATFORM_NAME + " v" + VERSION);
        Platform platform = new Platform(PLATFORM_NAME);
        try {
            ContentRepository.LoadResult result = REPOSITORY.load();
            result.errors().forEach(System.out::println);

            // A method reference (platform::addContent) can't be used here: lambdas can't throw checked exceptions
            for (Content content : result.contents()) {
                try {
                    platform.addContent(content);
                } catch (ExistingContentException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (NoSuchFileException e) {
            System.out.println("No catalog file yet (" + CATALOG_FILE + "): starting empty, it will be created when you add content.");
        } catch (IOException e) {
            System.out.println("Could not read " + CATALOG_FILE + ": " + e);
            canSave = false;
        }
        System.out.println("More of " + platform.totalDuration() + " minutes of content!");


        while (running) {
            System.out.println();
            // The menu is printed from the enum, so it can never differ from the switch below
            Arrays.stream(MenuOption.values()).forEach(System.out::println);

            long number = ScannerUtils.catchNumber("Choose an option");
            Optional<MenuOption> choice = MenuOption.fromNumber(number);

            if (choice.isEmpty()) {
                System.out.println("Invalid option");
                continue;
            }

            switch (choice.get()) {
                case ADD -> {
                    ContentType type = ScannerUtils.catchEnum("Type", ContentType.class);
                    String title = ScannerUtils.catchText("Title");
                    Genre genre = ScannerUtils.catchEnum("Genre", Genre.class);
                    Language language = ScannerUtils.catchEnum("Language", Language.class);
                    Quality quality = ScannerUtils.catchEnum("Quality", Quality.class);
                    long duration = ScannerUtils.catchNumber("Duration (minutes)");
                    double note = ScannerUtils.catchDecimals("Note (0-5)", 0, 5);

                    // Which subclass to create depends on the type chosen; the rest of the program only sees a Content
                    Content content = switch (type) {
                        case MOVIE -> new Movie(title, genre, language, quality, duration, note,
                                ScannerUtils.catchText("Director"));
                        case DOCUMENTARY -> {
                            // Inside a switch expression a block must "yield" its value (like return)
                            String topic = ScannerUtils.catchText("Topic");
                            String narrator = ScannerUtils.catchText("Narrator");
                            yield new Documentary(title, genre, language, quality, duration, note, topic, narrator);
                        }
                    };

                    try {
                        platform.addContent(content);
                        System.out.println("Added: " + title);
                        appendToFile(content);
                    } catch (ExistingContentException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case SHOW_ALL -> ConsoleUtils.showContent(platform.getContent(), "The catalog is empty");
                case FIND_BY_TITLE -> {
                    String search = ScannerUtils.catchText("Title to search");
                    ConsoleUtils.showContent(platform.findByTitle(search), "No movies found for: " + search);
                }
                case FIND_BY_GENRE -> {
                    Genre genre = ScannerUtils.catchEnum("Genre to search", Genre.class);
                    ConsoleUtils.showContent(platform.findByGenre(genre), "No movies found for genre: " + genre);
                }
                case FIND_BY_TYPE -> {
                    ContentType type = ScannerUtils.catchEnum("Type to search", ContentType.class);
                    List<Content> results = platform.findByType(type);

                    ConsoleUtils.showContent(results, "No " + type + " found");
                    // details() is polymorphic: each element answers with its own data (director, topic + narrator...)
                    results.forEach(content -> System.out.println(content.title() + " -> " + content.details()));
                }
                case TOP_RATED -> {
                    long top = ScannerUtils.catchNumber("top Nª:");
                    ConsoleUtils.showContent(platform.topRated(top), "No top rated movies yet");
                }
                case BY_NOTE -> {
                    double note = ScannerUtils.catchDecimals("Note (0-5)");
                    ConsoleUtils.showContent(platform.moviesByNote(note), "No movies found with note: " + note);
                }
                case SHORTEST_AND_LONGEST -> {
                    Optional<DurationRange> range = platform.durationRange();

                    ConsoleUtils.showContent(range.map(DurationRange::asList).orElse(List.of()), "The catalog is empty");
                    range.ifPresent(r -> System.out.println("Difference: " + r.differenceInMinutes() + " minutes"));
                }
                case PROMOTIONS -> {
                    List<Promotable> promotions = platform.promotions();

                    if (promotions.isEmpty()) {
                        System.out.println("Nothing to promote");
                    } else {
                        promotions.forEach(promotion -> System.out.println(promotion.banner()));
                    }
                }
                case PLAY -> {
                    String title = ScannerUtils.catchText("Exact title to play");

                    // ifPresentOrElse runs the first lambda if the Optional has a value, the second if it is empty
                    platform.play(title).ifPresentOrElse(
                            content -> {
                                ConsoleUtils.showContent(List.of(content), "");
                                System.out.println(content.details());
                                System.out.println("Views: " + platform.viewsOf(content));
                            },
                            () -> System.out.println("No content found with title: " + title));
                }
                case MOST_VIEWED -> {
                    Map<Content, Integer> ranking = platform.mostViewed();

                    if (ranking.isEmpty()) {
                        System.out.println("Nothing has been played yet");
                    } else {
                        // BiConsumer lambda: forEach on a Map gives (key, value) at once
                        ranking.forEach((content, count) -> System.out.println(count + " views - " + content.title()));
                    }
                }
                case STATS -> {
                    CatalogStats stats = platform.stats();
                    System.out.printf("Titles: %d | Total: %d min | Average length: %.0f min | Average note: %.2f%n",
                            stats.movieCount(), stats.totalMinutes(), stats.averageMinutes(), stats.averageNote());
                }
                case DELETE -> {
                    String title = ScannerUtils.catchText("Exact title to delete");

                    if (platform.removeByTitle(title)) {
                        System.out.println("Deleted: " + title);
                        deleteFromFile(title);
                    } else {
                        System.out.println("No content found with title: " + title);
                    }
                }
                case LOGOUT -> {
                    System.out.println("Logging out...");
                    running = false;
                }
            }
        }
    }

    /** Adds the content to the file. Called after every change so nothing depends on logging out properly. */
    private static void appendToFile(Content content) {
        if (!canSave) {
            return;
        }
        try {
            REPOSITORY.append(content);
        } catch (IOException e) {
            System.out.println("Could not save the catalog: " + e);
        }
    }

    private static void deleteFromFile(String title) {
        if (!canSave) {
            return;
        }
        try {
            REPOSITORY.delete(title);
        } catch (IOException e) {
            System.out.println("Could not save the catalog: " + e);
        }
    }
}
