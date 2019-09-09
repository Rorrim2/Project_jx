package sample;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class PackageToSend implements Serializable {
    byte[] content;
    String filename;
    String username;

    public PackageToSend(String user, String nameOfFile, File f) {
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
