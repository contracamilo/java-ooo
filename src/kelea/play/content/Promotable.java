package kelea.play.content;

/**
 * Something the platform can advertise. An interface defines a CAPABILITY ("can be promoted"), not what
 * something IS: any class can implement it, whatever it extends. Here only Movie does; a Documentary
 * could be added later by just writing "implements Promotable".
 */
public interface Promotable {

    /** Abstract method: every implementing class must write it. */
    String promoText();

    /**
     * Default method: an interface can bring ready-made code that implementers inherit for free
     * (and may override). It can only use the interface's own methods, since it knows nothing else about the class.
     */
    default String banner() {
        return "*** " + promoText() + " ***";
    }
}
