package kelea.play.repository;

import kelea.play.content.Content;

import java.io.IOException;
import java.util.List;

/**
 * Where the catalog is stored. The rest of the program depends on this interface, not on a file: to store
 * the catalog somewhere else (memory for tests, a database...) you write another implementation and nothing
 * else changes. Implementations throw IOException (checked) because storage can always fail.
 */
public interface ContentRepository {

    /** Contents read plus one message per entry that couldn't be read: a bad entry doesn't stop the load. */
    record LoadResult(List<Content> contents, List<String> errors) {
    }

    LoadResult load() throws IOException;

    void append(Content content) throws IOException;

    /** @return true if something was removed */
    boolean delete(String title) throws IOException;
}
