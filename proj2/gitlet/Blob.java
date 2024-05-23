package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import static gitlet.Utils.*;

public class Blob implements Serializable {
    public final String fileName;
    public final byte[] content;

    public Blob(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content;
    }

    /** Save this blob object(if not exists) to the given savePath, and return the blob hash. */
    public String saveBlob(File savePath) {
        String ref = sha1Hash(this);

        File saveFile = join(savePath, ref);
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writeObject(saveFile, this);
        }

        return ref;
    }
}
