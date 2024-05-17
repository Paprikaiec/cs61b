package gitlet;

import java.io.Serializable;

public class Blob implements Serializable {
    public String fileName;
    public byte[] content;

    public Blob(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content;
    }
}
