package library;


import java.io.*;
import java.util.*;

public class BookRepository {

    private final File booksFile;

    public BookRepository(String filePath) {
        this.booksFile = new File(filePath);
    }

    // تحميل كل الكتب من الملف
    private List<Book> loadAllBooks() {
        List<Book> books = new ArrayList<>();

        if (!booksFile.exists()) {
            return books; // ملف مش موجود → ما في كتب
        }

        try (BufferedReader br = new BufferedReader(new FileReader(booksFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // هنا افتراض إن الفاصل هو ; وعدد الحقول 4
                String[] parts = line.split(";");
                if (parts.length < 4) continue; // سطر معطوب

                String isbn   = parts[0].trim();
                String title  = parts[1].trim();
                String author = parts[2].trim();
                int copies;
                try {
                    copies = Integer.parseInt(parts[3].trim());
                } catch (NumberFormatException e) {
                    copies = 0;
                }

                books.add(new Book(isbn, title, author, copies));
            }
        } catch (IOException e) {
            System.out.println("Error reading books file: " + e.getMessage());
        }

        return books;
    }

    // بحث بالعنوان (جزئي، بدون حساسية لحالة الأحرف)
    public List<Book> searchByTitle(String titleFragment) {
        String q = titleFragment.toLowerCase();
        List<Book> result = new ArrayList<>();

        for (Book b : loadAllBooks()) {
            if (b.getTitle().toLowerCase().contains(q)) {
                result.add(b);
            }
        }
        return result;
    }

    // بحث بالمؤلف
    public List<Book> searchByAuthor(String authorFragment) {
        String q = authorFragment.toLowerCase();
        List<Book> result = new ArrayList<>();

        for (Book b : loadAllBooks()) {
            if (b.getAuthor().toLowerCase().contains(q)) {
                result.add(b);
            }
        }
        return result;
    }

    // بحث بالـ ISBN (غالبًا exact match)
    public List<Book> searchByIsbn(String isbn) {
        String q = isbn.trim();
        List<Book> result = new ArrayList<>();

        for (Book b : loadAllBooks()) {
            if (b.getIsbn().equals(q)) {
                result.add(b);
            }
        }
        return result;
    }
}
