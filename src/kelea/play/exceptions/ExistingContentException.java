package kelea.play.exceptions;

/**
 * Thrown when trying to add a content (movie or documentary) whose title is already in the catalog.
 * It extends Exception (not RuntimeException), so it is a *checked* exception: the compiler forces
 * every caller to either handle it with try/catch or declare it with "throws". That makes the
 * "duplicate movie" case impossible to forget.
 */
public class ExistingContentException extends Exception {

    private final String title;

    public ExistingContentException(String title) {
        // super(message) is what getMessage() returns later
        super("The content already exists: " + title);
        this.title = title;
    }

    /** Title that caused the conflict, in case the caller needs more than the message. */
    public String getTitle() {
        return title;
    }
}
