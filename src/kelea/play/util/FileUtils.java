package kelea.play.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Generic helpers to work with text files line by line. It knows nothing about movies: whoever calls it
 * decides what a line means. Every method throws IOException (checked) because disk access can always fail.
 *
 * Editing a text file in place isn't possible (changing one line's length shifts everything after it), so
 * edit and delete read the lines, change the list in memory and write the file again (safely).
 */
public final class FileUtils {

    // Utility class: only static members, so it must not be instantiated
    private FileUtils() {
    }

    /** Reads every line of the file (UTF-8). */
    public static List<String> readLines(Path file) throws IOException {
        return Files.readAllLines(file);
    }

    /**
     * Writes the lines replacing the previous content, creating the file and its folders if needed.
     * Safe write: everything goes to a temporary file first and is then moved over the real one. An atomic
     * move is all-or-nothing, so a crash in the middle can't leave the real file half written.
     */
    public static void writeLines(Path file, List<String> lines) throws IOException {
        createParentFolder(file);

        // resolveSibling: same folder as "file", different name -> data/movies.csv.tmp
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(temp, lines);
        Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE);
    }

    /** Adds one line at the end without rewriting the file (creates the file if it doesn't exist). */
    public static void appendLine(Path file, String line) throws IOException {
        createParentFolder(file);

        // If someone edited the file by hand and left no final line break, our line would stick to the last one
        String prefix = endsWithLineBreak(file) ? "" : System.lineSeparator();

        Files.writeString(file, prefix + line + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Edit: every line that matches is replaced by the result of applying "edit" to it.
     * The file is only written if something changed.
     *
     * @return how many lines were edited
     */
    public static int replaceLines(Path file, Predicate<String> matches, UnaryOperator<String> edit) throws IOException {
        List<String> lines = readLines(file);

        List<String> result = lines.stream()
                .map(line -> matches.test(line) ? edit.apply(line) : line)
                .toList();

        long changed = lines.stream().filter(matches).count();
        if (changed > 0) {
            writeLines(file, result);
        }
        return (int) changed;
    }

    /**
     * Delete: removes every line that matches. The file is only written if something was removed.
     *
     * @return how many lines were removed
     */
    public static int deleteLines(Path file, Predicate<String> matches) throws IOException {
        List<String> lines = readLines(file);

        List<String> kept = lines.stream()
                .filter(matches.negate())
                .toList();

        int removed = lines.size() - kept.size();
        if (removed > 0) {
            writeLines(file, kept);
        }
        return removed;
    }

    /** Deletes the whole file; returns false if it didn't exist (instead of throwing). */
    public static boolean deleteFile(Path file) throws IOException {
        return Files.deleteIfExists(file);
    }

    private static void createParentFolder(Path file) throws IOException {
        // Does nothing if the folder already exists. toAbsolutePath avoids a null parent for names like "a.csv".
        Files.createDirectories(file.toAbsolutePath().getParent());
    }

    /** Looks at the last byte only (a channel can jump to any position), without reading the whole file. */
    private static boolean endsWithLineBreak(Path file) throws IOException {
        if (!Files.exists(file) || Files.size(file) == 0) {
            return true;
        }
        try (SeekableByteChannel channel = Files.newByteChannel(file)) {
            channel.position(channel.size() - 1);
            ByteBuffer buffer = ByteBuffer.allocate(1);
            channel.read(buffer);
            return buffer.get(0) == '\n';
        }
    }
}
