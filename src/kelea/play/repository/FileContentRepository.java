package kelea.play.repository;

import kelea.play.content.Content;
import kelea.play.content.ContentType;
import kelea.play.content.Documentary;
import kelea.play.content.Genre;
import kelea.play.content.Language;
import kelea.play.content.Movie;
import kelea.play.content.Quality;
import kelea.play.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * ContentRepository that keeps the catalog in a plain text file, one content per line, fields separated by ';':
 *
 *   title;genre;language;quality;duration;note[;type[;extra]]
 *
 * - 6 fields (no type): a movie. Old files written before documentaries existed still load.
 * - type MOVIE: 7 fields, or 8 with the director as the last one.
 * - type DOCUMENTARY: 8 fields (topic), or 9 with the narrator as the last one.
 * The title is always the first field, which is what delete() relies on.
 *
 * Uses NIO.2 (java.nio.file): Path says WHERE a file is, Files does the work (read, write, copy, check...).
 */
public final class FileContentRepository implements ContentRepository {

    private static final String SEPARATOR = ";";
    private static final int BASE_FIELDS = 6;
    private static final String HEADER = "# title;genre;language;quality;duration(min);note(0-5);type;director(movies)/topic;narrator(documentaries)";

    private final Path file;

    public FileContentRepository(Path file) {
        this.file = file;
    }

    /**
     * @throws IOException if the file doesn't exist or can't be read. It is a checked exception: reading
     *                     a file can always fail for reasons outside the program, so the compiler makes you handle it.
     */
    @Override
    public LoadResult load() throws IOException {
        // readLines loads the whole file into memory. Fine for small files; for big ones, Files.lines(file)
        // gives a lazy Stream<String> (it must be closed: use try-with-resources).
        List<String> lines = FileUtils.readLines(file);

        List<Content> contents = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            int lineNumber = i + 1; // lists start at 0, people count lines from 1

            // Blank lines and comments (#) are ignored
            if (!isDataLine(line)) {
                continue;
            }

            try {
                contents.add(parse(line));
            } catch (IllegalArgumentException e) {
                // Also catches NumberFormatException (a subclass), unknown enum names and the constructors' validation
                errors.add("Line " + lineNumber + " skipped: " + e.getMessage());
            }
        }
        return new LoadResult(contents, errors);
    }

    private static Content parse(String line) {
        String[] fields = line.split(SEPARATOR);
        if (fields.length < BASE_FIELDS) {
            throw new IllegalArgumentException("expected at least " + BASE_FIELDS + " fields but found " + fields.length);
        }

        // valueOf throws IllegalArgumentException when the text is not a constant of the enum
        ContentType type = fields.length == BASE_FIELDS
                ? ContentType.MOVIE
                : ContentType.valueOf(fields[6].trim().toUpperCase());

        // Optional trailing fields make several lengths valid for each type
        boolean validLength = switch (type) {
            case MOVIE -> fields.length <= BASE_FIELDS + 2;                    // 6 (legacy), 7 (type) or 8 (+ director)
            case DOCUMENTARY -> fields.length >= BASE_FIELDS + 2               // 8 (+ topic) or 9 (+ narrator)
                    && fields.length <= BASE_FIELDS + 3;
        };
        if (!validLength) {
            throw new IllegalArgumentException(type + " can't have " + fields.length + " fields");
        }

        String title = fields[0].trim();
        Genre genre = Genre.valueOf(fields[1].trim().toUpperCase());
        Language language = Language.valueOf(fields[2].trim().toUpperCase());
        Quality quality = Quality.valueOf(fields[3].trim().toUpperCase());
        long duration = Long.parseLong(fields[4].trim());
        double note = Double.parseDouble(fields[5].trim());

        // Switch expression over the enum: the compiler checks that every type is handled
        return switch (type) {
            case MOVIE -> fields.length == BASE_FIELDS + 2
                    ? new Movie(title, genre, language, quality, duration, note, fields[7].trim())
                    : new Movie(title, genre, language, quality, duration, note);
            case DOCUMENTARY -> fields.length == BASE_FIELDS + 3
                    ? new Documentary(title, genre, language, quality, duration, note, fields[7].trim(), fields[8].trim())
                    : new Documentary(title, genre, language, quality, duration, note, fields[7].trim());
        };
    }

    /** Adds one content at the end of the file; creates the file (with its header) if it doesn't exist. */
    @Override
    public void append(Content content) throws IOException {
        if (!Files.exists(file)) {
            FileUtils.writeLines(file, List.of(HEADER));
        }
        FileUtils.appendLine(file, format(content));
    }

    /**
     * Deletes the content with that title (ignoring case) from the file, leaving every other line untouched,
     * including comments and lines that couldn't be read.
     *
     * @return true if a line was removed
     */
    @Override
    public boolean delete(String title) throws IOException {
        String wanted = title.trim();
        return FileUtils.deleteLines(file, line -> isDataLine(line)
                && line.split(SEPARATOR)[0].trim().equalsIgnoreCase(wanted)) > 0;
    }

    /** Lines that hold data: not blank and not a comment. */
    private static boolean isDataLine(String line) {
        String trimmed = line.trim();
        return !trimmed.isEmpty() && !trimmed.startsWith("#");
    }

    /** Inverse of parse(). name() is the enum constant name (SCI_FI), which valueOf() reads back. */
    private static String format(Content content) {
        String base = String.join(SEPARATOR,
                clean(content.title()),
                content.genre().name(),
                content.language().name(),
                content.quality().name(),
                String.valueOf(content.duration()),
                String.valueOf(content.note()),
                content.type().name());

        // Pattern matching in a switch: "case Documentary d" checks the type AND gives it already cast.
        // Content is sealed, so these two cases are all there is and no "default" is needed.
        return switch (content) {
            case Movie movie -> base + SEPARATOR + clean(movie.director());
            case Documentary documentary -> base + SEPARATOR + clean(documentary.topic()) + SEPARATOR + clean(documentary.narrator());
        };
    }

    /** A ';' inside a text would be read back as a field separator, so it is replaced. */
    private static String clean(String text) {
        return text.replace(SEPARATOR, ",");
    }
}
