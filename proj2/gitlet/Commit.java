package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.text.SimpleDateFormat;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  @author wyw
 */
public class Commit implements Serializable {
    /** The date of this Commit. */
    public final Date timestamp;
    /** The message of this Commit. */
    public final String message;
    /** The consisting blobs<file name, blob hash> of this Commit. */
    public final HashMap<String, String> blobs;
    /** The parent reference of this Commit. */
    public final String parent;
    /** The second parent(for merge) reference of this Commit. */
    public final String secondParent;

    public Commit(Date timestamp, String message, HashMap<String, String> blobs,
                  String parent, String secondParent) {
        this.timestamp = timestamp;
        this.message = message;
        this.blobs = blobs;
        this.parent = parent;
        this.secondParent = secondParent;
    }
}
