package library.domain.media;

/**
 * Represents a book media item in the library.
 * Stores the title, author, and ISBN.
 */
public class Book extends Media {

    private final String isbn;
    private final String author;

    /**
     * Creates a new Book.
     *
     * @param title  the book title
     * @param author the author's name
     * @param isbn   the ISBN of the book
     */
    public Book(String title, String author, String isbn) {
        super(title, "BOOK");
        this.author = author;
        this.isbn = isbn;
    }

    /** @return the author's name */
    public String getAuthor() { return author; }

    /** @return the ISBN value */
    public String getIsbn() { return isbn; }
}
