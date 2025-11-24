package library;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class Login {
	
	public static final Path ADMINS_FILE = Paths.get("src", "main", "resources", "admins.csv");
	public static final Path USERS_FILE = Paths.get("src", "main", "resources", "users.csv");
    public  boolean authenticate(int id, String password, String role) {
        if (role == null) return false;
        String r = role.trim().toUpperCase(Locale.ROOT);

        Path fileToCheck;
        if (r.equals("ADMIN")) {
            fileToCheck = ADMINS_FILE;
        } else if (r.equals("USER")) {
            fileToCheck = USERS_FILE;
        } else {
           
            return false;
        }

       
        if (!Files.exists(fileToCheck)) {
            return false;
        }

        try (BufferedReader br = Files.newBufferedReader(fileToCheck)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue; // skip comments/empty
                String[] parts = line.split(",");
                if (parts.length < 3) continue; // need at least id,username,password

                int fileId;
                try {
                    fileId = Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException ex) {
                    continue; 
                }

                String filePassword = parts[2].trim();

                if (fileId == id && filePassword.equals(password)) {
                    return true; // match found
                }
            }
        } catch (IOException e) {
            // On IO error, treat as authentication failure. Up to caller to log/notify.
            return false;
        }

        return false;
    }
    public  boolean authenticate(String idText, String password, String role) {
        try {
            int id = Integer.parseInt(idText.trim());
            return authenticate(id, password, role);
        } catch (NumberFormatException ex) {
            return false;
        }
    }
   

}
