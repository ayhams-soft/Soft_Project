package library.domain.media;

/**
 * Represents a CD media item in the library.
 * Stores the title and the artist name.
 */
public class CD extends Media {

    private final String artist;

    /**
     * Creates a new CD.
     *
     * @param title  the CD title
     * @param artist the artist or band name
     */
    public CD(String title, String artist) {
        super(title, "CD");
        this.artist = artist;
    }

    /** @return the artist name */
    public String getArtist() { return artist; }
}
