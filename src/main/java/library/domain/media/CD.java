package library.domain.media;

/**
 * CD media.
 */
public class CD extends Media {
    private final String artist;

    public CD(String title, String artist) {
        super(title, "CD");
        this.artist = artist;
    }

    public String getArtist() { return artist; }
}
