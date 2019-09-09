package server;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

public class PackageGiven implements Serializable {
    byte[] content;
    String filename;
    String username;

    public PackageGiven(String user, String nameOfFile, File f) {
        this.username = user;
        this.filename = nameOfFile;
        try {
            this.content = Files.readAllBytes(f.toPath());
        }
        catch(IOException io) {
            System.out.println(io.getStackTrace());
        }
    }

    public byte[] getContent() {
        return content;
    }

    public String getFilename() {
        return filename;
    }

    public String getUsername() {
        return username;
    }
}
