package library.domain.media;


public abstract class Media {
    private static long SEQ = 1;
    private final String id;
    private final String title;
    private boolean available = true;
    private final String mediaType;

    protected Media(String title, String mediaType) {
        this.id = "M" + (SEQ++);
        this.title = title;
        this.mediaType = mediaType;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getMediaType() { return mediaType; }
}
