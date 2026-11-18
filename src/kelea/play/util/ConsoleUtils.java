package kelea.play.util;

import kelea.play.content.Content;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Everything that prints to the console lives here, so Platform (the domain) stays free of output
 * formatting and Main doesn't repeat the same "if empty -> message, else -> table" block.
 */
public final class ConsoleUtils {

    private static final String ROW = "| %-3s | %-28s | %-11s | %-12s | %-10s | %-7s | %8s | %5s |%n";
    private static final String SEPARATOR = "+-----+------------------------------+-------------+--------------+------------+---------+----------+-------+";

    // Utility class: only static members, so it must not be instantiated
    private ConsoleUtils() {
    }

    /** Prints the content as a table, or the given message when the list is empty. */
    public static void showContent(List<Content> movies, String emptyMessage) {
        if (movies.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }

        System.out.println(SEPARATOR);
        System.out.printf(ROW, "#", "Title", "Type", "Genre", "Language", "Quality", "Duration", "Note");
        System.out.println(SEPARATOR);

        // Streams have no built-in index, so we stream the positions (0..size-1) and look the movie up by each one
        IntStream.range(0, movies.size())
                .mapToObj(i -> formatRow(i + 1, movies.get(i)))
                .forEach(System.out::print);

        System.out.println(SEPARATOR);
    }

    /** Builds one table row as a String. In printf, "%-28s" pads to 28 chars aligned left; "%8s" aligns right. */
    private static String formatRow(int position, Content movie) {
        return String.format(ROW,
                position,
                truncate(movie.title(), 28),
                truncate(movie.type().getLabel(), 11),
                truncate(movie.genre().getLabel(), 12),
                truncate(movie.language().getLabel(), 10),
                truncate(movie.quality().getLabel(), 7),
                movie.duration() + " min",
                String.format("%.1f", movie.note()));
    }

    /** Cuts text that doesn't fit its column so it can't break the table alignment. Null-safe. */
    private static String truncate(String text, int max) {
        if (text == null) {
            return "-";
        }
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }
}
