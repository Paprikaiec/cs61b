package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import static gitlet.Utils.*;

public class Blob implements Serializable {
    public String fileName;
    public byte[] content;

    public Blob(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content;
    }

    /** Save this blob object to the given savePath, and return the sha1 code. */
    public String saveBlob(File savePath) {
        String ref = sha1(this);

        File saveFile = join(savePath, ref);
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeObject(saveFile, this);

        return ref;
    }
}
