package kelea.play;

import java.util.Optional;

/**
 * Options of the main menu. The declaration order defines the number shown to the user (first = 1),
 * so adding, removing or moving an option never leaves the numbers out of sync with the switch in Main.
 */
public enum MenuOption {
    ADD("Add title"),
    SHOW_ALL("Show all"),
    FIND_BY_TITLE("Find by title"),
    FIND_BY_GENRE("Find by genre"),
    FIND_BY_TYPE("Find by type"),
    TOP_RATED("Top rated"),
    BY_NOTE("Movies by note"),
    SHORTEST_AND_LONGEST("Shortest and longest"),
    PROMOTIONS("Promotions"),
    PLAY("Play"),
    MOST_VIEWED("Most viewed"),
    STATS("Catalog summary"),
    DELETE("Delete"),
    LOGOUT("Logout");

    private final String label;

    MenuOption(String label) {
        this.label = label;
    }

    /** Converts the number typed by the user into an option; empty if it is out of range. */
    public static Optional<MenuOption> fromNumber(long number) {
        MenuOption[] options = values();
        if (number < 1 || number > options.length) {
            return Optional.empty();
        }
        return Optional.of(options[(int) number - 1]);
    }

    /** Menu line, e.g. "3. Find by title". ordinal() is the position in the declaration (starts at 0). */
    @Override
    public String toString() {
        return (ordinal() + 1) + ". " + label;
    }
}
