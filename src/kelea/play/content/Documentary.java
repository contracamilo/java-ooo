package kelea.play.content;

import java.time.LocalDate;
import java.util.Objects;

/** A documentary: a Content that also has a topic and a narrator. */
public final class Documentary extends Content {

    /** Used for documentaries loaded from files written before narrators existed. */
    public static final String UNKNOWN_NARRATOR = "Unknown";

    private final String topic;
    private final String narrator;

    public Documentary(String title, String desc, Genre genre, Language language, Quality quality,
                       LocalDate releaseDate, double note, boolean available, long duration, String topic,
                       String narrator) {
        super(title, desc, genre, language, quality, releaseDate, note, available, duration);
        this.topic = Objects.requireNonNull(topic, "topic is required");
        if (topic.isBlank()) {
            throw new IllegalArgumentException("topic can't be blank");
        }
        this.narrator = Objects.requireNonNull(narrator, "narrator is required");
        if (narrator.isBlank()) {
            throw new IllegalArgumentException("narrator can't be blank");
        }
    }

    /** Short constructor for the data we always have. */
    public Documentary(String title, Genre genre, Language language, Quality quality,
                       long duration, double note, String topic, String narrator) {
        this(title, null, genre, language, quality, null, note, false, duration, topic, narrator);
    }

    /** Short constructor when the narrator is not known. */
    public Documentary(String title, Genre genre, Language language, Quality quality,
                       long duration, double note, String topic) {
        this(title, genre, language, quality, duration, note, topic, UNKNOWN_NARRATOR);
    }

    public String topic() {
        return topic;
    }

    public String narrator() {
        return narrator;
    }

    @Override
    public ContentType type() {
        return ContentType.DOCUMENTARY;
    }

    @Override
    public void play() {
        // Same call as Movie.play(), different behavior: that is polymorphism
        System.out.println("playing documentary " + title() + " about " + topic + ", narrated by " + narrator);
    }

    @Override
    public String details() {
        return "Topic: " + topic + " | Narrator: " + narrator;
    }

    @Override
    public Documentary withAvailable(boolean available) {
        return new Documentary(title(), desc(), genre(), language(), quality(), releaseDate(), note(), available,
                duration(), topic, narrator);
    }
}
