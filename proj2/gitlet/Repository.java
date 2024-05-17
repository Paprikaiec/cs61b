package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

// TODO: any imports you need here
import static gitlet.Utils.*;
import gitlet.Commit;
import org.checkerframework.checker.units.qual.C;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author wyw
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The commits file. */
    public static final File COMMITS_FILE = join(GITLET_DIR, "commits");
    /** The current head pointer file(store branch name). */
    public static final File HEAD_FILE = join(GITLET_DIR, "head");
    /** The branches file(store hash map for branch pointer name and reference). */
    public static final File BRANCHES_FILE = join(GITLET_DIR, "branches");
    /** The blobs directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The Staging Area directory. */
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    /** The Staging file references map(store hash map for staging blobs) */
    public static final File STAGINGREFSMAP_FILE = join(GITLET_DIR, "stagingRefsMap");

    /* TODO: fill in the rest of this class. */
    public static void setupPersistence() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }

        if (!COMMITS_FILE.exists()) {
            try {
                COMMITS_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!BRANCHES_FILE.exists()) {
            try {
                BRANCHES_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!HEAD_FILE.exists()) {
            try {
                HEAD_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!BLOBS_DIR.exists()) {
            BLOBS_DIR.mkdir();
        }

        if (!STAGING_DIR.exists()) {
            STAGING_DIR.mkdir();
        }

        if (!STAGINGREFSMAP_FILE.exists()) {
            try {
                STAGINGREFSMAP_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void gitletInit() {
        if (GITLET_DIR.exists()) {
            exitWithError("A Gitlet version-control system already exists in the current directory.");
        }

        setupPersistence();

        // Set new hash map to store commits.
        HashMap<String, Commit> commits = new HashMap<>();
        Commit iniCommit = new Commit(new Date(0), "initial commit", null, null, null);
        String iniKey = sha1(iniCommit);
        commits.put(iniKey, iniCommit);
        writeObject(COMMITS_FILE, commits);

        // Set new HEAD point to the initial commit.
        String head = "master";
        writeContents(HEAD_FILE, head);

        // Set new hash map to store branches reference name.
        HashMap<String, String> branches = new HashMap<>();
        branches.put(head, iniKey);
        writeObject(BRANCHES_FILE, branches);

        //Set stagingRefsMap file to store staging references.
        HashMap<>

    }



    public static void gitletAdd(String fileName) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        // Judge whether the fileName file exist.
        List<String> currentFiles = plainFilenamesIn(GITLET_DIR);
        if (!currentFiles.contains(fileName)) {
            exitWithError("File does not exist.");
        }

        // Read file and store blob and set staging reference.
        Blob stagingBlob = new Blob(fileName, readContents(join(CWD, fileName)));
        String stagingRef = sha1(stagingBlob);


    }

    private static void createBranch(String name, String commitRef) {

    }
}
