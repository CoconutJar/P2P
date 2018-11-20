package settings;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// FileObject class used to easily update/process data of file tables and communication
public class FileObject {
    private StringProperty filename, description, hostname, speed;
    private int size;

    // Constructor used for FileObjects in MyFiles Table
    public FileObject(String filename, int size, String description){
        this.filename = new SimpleStringProperty(filename);
        this.size = size;
        this.description = new SimpleStringProperty(description);
    }

    public String getFilename() {
        return filename.get();
    }
    public String getDescription() {
        return description.get();
    }
    public void setDescription(String description) {
        this.description.set(description);
    }

    // Constructor used for FileObjects in Server File Table
    public FileObject(String filename, String hostname, String speed, String description){
        this.filename = new SimpleStringProperty(filename);
        this.hostname = new SimpleStringProperty(hostname);
        this.speed = new SimpleStringProperty(speed);
        this.description = new SimpleStringProperty(description);
    }

    public String getHostname() {
        return hostname.get();
    }
    public String getSpeed() {
        return speed.get();
    }


}
