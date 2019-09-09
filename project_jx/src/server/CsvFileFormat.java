package server;

public class CsvFileFormat {
    private String name;
    private String owner;
    private String shared;
    private String sharedFrom;

    private String folderNumber;

    public CsvFileFormat(String name, String owner, String folderNumber, String shared, String sharedFrom) {
        this.name = name;
        this.owner = owner;
        this.folderNumber = folderNumber;
        this.shared = shared;
        this.sharedFrom = sharedFrom;
    }

    public String getFolderNumber() {
        return folderNumber;
    }

    public void setFolderNumber(String folderNumber) {
        this.folderNumber = folderNumber;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getShared() {
        return shared;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShared(String shared) {
        this.shared = shared;
    }

    public String getSharedFrom() {
        return sharedFrom;
    }

    public void setSharedFrom(String sharedFrom) {
        this.sharedFrom = sharedFrom;
    }
}
