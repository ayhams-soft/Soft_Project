package library;


public class Book {
    private String isbn;
    private String title;
    private String author;
    private int availableCopies;

    public Book(String isbn, String title, String author, int availableCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.availableCopies = availableCopies;
    }

    public String getIsbn()   {
    	return isbn;
    	}
    public String getTitle()  { 
    	return title;
    	}
    public String getAuthor() {
    	return author;
    	}
    public int getAvailableCopies() {
        return availableCopies;
    }

   
}
