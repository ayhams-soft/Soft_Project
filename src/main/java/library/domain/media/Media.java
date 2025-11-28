package library.domain.media;

/**
 * Base class for all media items in the library (e.g., books, CDs).
 * Each media has an auto-generated id, a title, a type, and an availability flag.
 */
public abstract class Media {

    private static long SEQ = 1;
    private final String id;
    private final String title;
    private boolean available = true;
    private final String mediaType;

    /**
     * Creates a new media item.
     *
     * @param title     the title of the media
     * @param mediaType the type of media (e.g., "BOOK", "CD")
     */
    protected Media(String title, String mediaType) {
        this.id = "M" + (SEQ++);
        this.title = title;
        this.mediaType = mediaType;
    }

    /** @return media id */
    public String getId() { return id; }

    /** @return media title */
    public String getTitle() { return title; }

    /** @return true if the media is available to borrow */
    public boolean isAvailable() { return available; }

    /** Sets the availability of the media item. */
    public void setAvailable(boolean available) { this.available = available; }

    /** @return the media type (BOOK, CD, etc.) */
    public String getMediaType() { return mediaType; }
}
