package library.domain.media;

/**
 * Book media.
 */
public class Book extends Media {
    private final String isbn;
    private final String author;

    public Book(String title, String author, String isbn) {
        super(title, "BOOK");
        this.author = author;
        this.isbn = isbn;
    }

    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
}
