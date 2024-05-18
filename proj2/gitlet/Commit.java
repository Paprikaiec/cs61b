package gitlet;

// TODO: any imports you need here

import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.List;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The date of this Commit. */
    public Date timestamp;
    /** The message of this Commit. */
    public String message;
    /** The consisting blobs<file name, blob hash> of this Commit. */
    public HashMap<String, String> blobs;
    /** The parent reference of this Commit. */
    public String parent;
    /** The second parent(for merge) reference of this Commit. */
    public String secondParent;
    /* TODO: fill in the rest of this class. */

    public Commit(Date timestamp, String message, HashMap<String, String> blobs, String parent, String secondParent) {
        this.timestamp = timestamp;
        this.message = message;
        this.blobs = blobs;
        this.parent = parent;
        this.secondParent = secondParent;
    }
}
