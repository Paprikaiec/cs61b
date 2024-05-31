package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import static gitlet.Utils.*;

public class Blob implements Serializable, Dumpable {
    public final String fileName;
    public final String contents;

    public Blob(String fileName, File file) {
        this.fileName = fileName;
        this.contents = readContentsAsString(file);
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

    /** Write this blob contents(if already exists, overwrite) to the given savePath. */
    public void writeBlobContents(File savePath) {
        File saveFile = join(savePath, fileName);
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(saveFile, contents);
    }

    /** Use for debug.*/
    @Override
    public void dump() {
        System.out.printf("File Name: %s%n", fileName);
        System.out.printf("contents: %s%n", contents);
    }
}
