package library;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;


public class Admin {

    private int id;
    private String username;
    private String passwordHash;
    private String email;

    public Admin(int id, String username, String passwordHash, String email) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    
    public boolean addBook(String title, String author, String isbn, int availableCopies) {
        if (title == null || author == null || isbn == null) {
            throw new IllegalArgumentException("title, author and isbn must not be null");
        }
        if (availableCopies < 0) {
            throw new IllegalArgumentException("availableCopies must be >= 0");
        }

        Path booksPath = Paths.get("src", "main", "resources", "books.csv");

        try {
            Files.createDirectories(booksPath.getParent());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // If file exists -> check duplicate ISBN
        try {
            if (Files.exists(booksPath)) {
                try (BufferedReader br = Files.newBufferedReader(booksPath, StandardCharsets.UTF_8)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        String[] parts = splitCsvLine(line);
                        if (parts.length >= 1) {
                            String existingIsbn = parts[0].trim();
                            if (existingIsbn.equals(isbn.trim())) {
                                // ISBN exists -> cannot add as new
                                return false;
                            }
                        }
                    }
                }
            } else {
                // create file with header
                try (BufferedWriter bw = Files.newBufferedWriter(booksPath, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    bw.write("# isbn,title,author,availableCopies");
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Append new book
        String safeIsbn = escapeCsv(isbn);
        String safeTitle = escapeCsv(title);
        String safeAuthor = escapeCsv(author);
        String newLine = String.format("%s,%s,%s,%d", safeIsbn, safeTitle, safeAuthor, availableCopies);

        try (BufferedWriter bw = Files.newBufferedWriter(booksPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(newLine);
            bw.newLine();
            bw.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    
    public boolean addCopies(String isbn, int extraCopies) {
        if (isbn == null) {
            throw new IllegalArgumentException("isbn must not be null");
        }
        if (extraCopies <= 0) {
            throw new IllegalArgumentException("extraCopies must be > 0");
        }

        Path booksPath = Paths.get("src", "main", "resources", "books.csv");

        List<String> updatedLines = new ArrayList<>();
        boolean found = false;

        try {
            if (!Files.exists(booksPath)) {
                return false;
            }

            try (BufferedReader br = Files.newBufferedReader(booksPath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String orig = line;
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        updatedLines.add(orig);
                        continue;
                    }

                    String[] parts = splitCsvLine(trimmed);
                    if (parts.length >= 4) {
                        String existingIsbn = parts[0].trim();
                        if (existingIsbn.equals(isbn.trim())) {
                            int current = 0;
                            try {
                                current = Integer.parseInt(parts[3].trim());
                            } catch (Exception e) {
                                current = 0;
                            }

                            int updatedCount = current + extraCopies;

                            // keep title and author from file, update count only
                            String safeIsbn = escapeCsv(parts[0]);
                            String safeTitle = escapeCsv(parts[1]);
                            String safeAuthor = escapeCsv(parts[2]);

                            String newLine = String.format("%s,%s,%s,%d",
                                    safeIsbn, safeTitle, safeAuthor, updatedCount);

                            updatedLines.add(newLine);
                            found = true;
                            continue;
                        }
                    }

                    updatedLines.add(orig);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!found) return false;

        // Write back entire file
        try {
            Files.write(booksPath, updatedLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** reuse previous escape helper */
    private String escapeCsv(String field) {
        if (field == null) return "";
        boolean needQuotes = field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r");
        String escaped = field.replace("\"", "\"\""); // double internal quotes
        if (needQuotes) {
            return "\"" + escaped + "\"";
        } else {
            return escaped;
        }
    }

    /** Simple CSV splitter that handles quoted fields (basic) */
    private String[] splitCsvLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // handle double quote inside quoted field
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++; // skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        parts.add(cur.toString());
        return parts.toArray(new String[0]);
    }
}
