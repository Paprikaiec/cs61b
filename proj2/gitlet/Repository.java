package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;
//import java.util.Collections;

// TODO: any imports you need here
import static gitlet.Utils.*;
import java.text.SimpleDateFormat;

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
    /** The staging file for addition references map(store hash map for staging blobs) */
    public static final File ADDMAP_FILE = join(GITLET_DIR, "addMap");
    /** The staging file for removal name set */
    public static final File RMSET_FILE = join(GITLET_DIR, "rmSet");

    /** The hash map of commits<commit hash, commit>(need prepareRef() to create). */
    public static HashMap<String, Commit> commits;
    /** The hash map of branches<branch name, commit hash>(need prepareRef() to create). */
    public static HashMap<String, String> branches;
    /** The current branch name(need prepareRef() to create). */
    public static String head;
    /** The current commit hash(need prepareRef() to create). */
    public static String currentCommitHash;
    /** The current commit object(need prepareRef() to create). */
    public static Commit currentCommit;
    /** The hash map of Staging references and file for addition<file name, blob hash>(need prepareRef() to create). */
    public static HashMap<String, String> addMap;
    /** The hash set of Staging file name for removal. */
    public static HashSet<String> rmSet;
    /* TODO: fill in the rest of this class. */
    private static void setupPersistence() {
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
        
        if (!ADDMAP_FILE.exists()) {
            try {
                ADDMAP_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!RMSET_FILE.exists()) {
            try {
                RMSET_FILE.createNewFile();
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
        commits = new HashMap<>();
        Commit iniCommit = new Commit(new Date(0), "initial commit", new HashMap<>(), null, null);
        String iniHash = sha1Hash(iniCommit);
        commits.put(iniHash, iniCommit);

        // Set new HEAD point to the initial commit.
        head = "master";

        // Set new hash map to store branches reference name.
        branches = new HashMap<>();
        branches.put(head, iniHash);

        // Set addMap file to store file for addition.
        addMap = new HashMap<>();

        // Set rmSet file to store file for removal.
        rmSet = new HashSet<>();

        saveRefs();
    }

    /** Read references from the corresponding files. */
    private static void loadRefs() {
        commits = readObject(COMMITS_FILE, HashMap.class);
        branches = readObject(BRANCHES_FILE, HashMap.class);
        head = readContentsAsString(HEAD_FILE);
        addMap = readObject(ADDMAP_FILE, HashMap.class);
        rmSet = readObject(RMSET_FILE, HashSet.class);
        currentCommitHash = branches.get(head);
        currentCommit = commits.get(currentCommitHash);


    }

    private static void saveRefs() {
        writeObject(COMMITS_FILE, commits);
        writeObject(BRANCHES_FILE, branches);
        writeContents(HEAD_FILE, head);
        writeObject(ADDMAP_FILE, addMap);
        writeObject(RMSET_FILE, rmSet);
    }

    public static void gitletAdd(String fileName) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        // Judge whether the fileName file exist.
        if (!dirContainsFile(CWD,fileName)) {
            exitWithError("File does not exist.");
        }

        loadRefs();

        // If the file name already exists in the rmSet, cancel the removal.
        rmSet.remove(fileName);
        // If the blobs area already exists the staged same name file, delete to overwrite.
        if (addMap.containsKey(fileName)) {
            join(BLOBS_DIR, addMap.get(fileName)).delete();
        }
        // Read file and store blob.
        Blob stagingBlob = new Blob(fileName, readContents(join(CWD, fileName)));
        String stagingRef = stagingBlob.saveBlob(BLOBS_DIR);
        // Set staging reference.
        addMap.put(fileName, stagingRef);

        // If the staged file is identical to the commited file, delete the addMap references.
        if (stagingRef.equals(currentCommit.blobs.get(fileName))) {
            addMap.remove(fileName);
        }

        saveRefs();
    }

    public static void gitletCommit(String message) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        if (message.isBlank()) {
            exitWithError("Please enter a commit message.");
        }

        loadRefs();

        if (addMap.isEmpty() && rmSet.isEmpty()) {
            exitWithError("No changes added to the commit.");
        }

        // Update commits.
        HashMap<String, String> blobs = currentCommit.blobs;
        for (String key : rmSet) {
            blobs.remove(key);
        }
        blobs.putAll(addMap);

        Commit commit = new Commit(new Date(), message, blobs, currentCommitHash, null);
        String commitHash = sha1Hash(commit);
        commits.put(commitHash, commit);

        // Update branches.
        branches.put(head, commitHash);

        // Update addMap and rmSet(clear).
        addMap.clear();
        rmSet.clear();

        // Store changes.
        saveRefs();
    }

    public static void gitletRm(String fileName) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();
        if (!addMap.containsKey(fileName) && !currentCommit.blobs.containsKey(fileName)) {
            exitWithError("No reason to remove the file.");
        }

        addMap.remove(fileName);

        if (currentCommit.blobs.containsKey(fileName)) {
            rmSet.add(fileName);
            if (dirContainsFile(CWD, fileName)) {
                restrictedDelete(join(CWD, fileName));
            }

        }

        saveRefs();
    }

    private static void printCommit(String cmtHash, Commit cmt) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        System.out.println("===");
        System.out.printf("commit %s%n", cmtHash);
        if (cmt.secondParent != null) {
            System.out.printf("Merge: %.7s %.7s%n", cmt.parent, cmt.secondParent);
        }
        System.out.println("Date: " + sdf.format(cmt.timestamp));
        System.out.println(cmt.message);
        System.out.println();
    }

    public static void gitletLog() {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        Commit cmt = currentCommit;
        String cmtHash = currentCommitHash;
        printCommit(cmtHash, cmt);
        while (cmt.parent != null) {
            cmtHash = cmt.parent;
            cmt = commits.get(cmt.parent);
            printCommit(cmtHash, cmt);
        }
    }

    public static void gitletGlobalLog() {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        for (String cmtHash : commits.keySet()) {
            printCommit(cmtHash, commits.get(cmtHash));
        }
    }

    public static void gitletFind(String message) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        Boolean fd = false;

        for (String cmtHash : commits.keySet()) {
            if (message.equals(commits.get(cmtHash).message)) {
                System.out.println(cmtHash);
                fd = true;
            }
        }

        if (!fd) {
            exitWithError("Found no commit with that message.");
        }

    }

    public static void gitletStatus() {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        System.out.println("=== Branches ===");
        TreeSet<String> branchesFiles = new TreeSet<>(branches.keySet());
        for (String name : branchesFiles) {
            if (name.equals(head)) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        TreeSet<String> addFiles = new TreeSet<>(addMap.keySet());
        for (String name : addFiles) {
                System.out.println(name);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        TreeSet<String> rmFiles = new TreeSet<>(rmSet);
        for (String name : rmFiles) {
            System.out.println(name);
        }
        System.out.println();

        // Filtering out modified files, deleted files, untracked files.
        List<String> currentFiles = plainFilenamesIn(CWD);
        TreeSet<String> commitedFiles = new TreeSet<>(currentCommit.blobs.keySet());

        TreeSet<String> modified = new TreeSet<>();
        TreeSet<String > deleted = new TreeSet<>();
        TreeSet<String> untracked = new TreeSet<>();
        
        if (currentFiles != null) {
            for (String fileName : currentFiles) {
                // Prepare probably deleted fileName.
                commitedFiles.remove(fileName);
                addFiles.remove(fileName);
                // Judge whether is untracked.
                if (rmSet.contains(fileName) ||
                        !currentCommit.blobs.containsKey(fileName) && !addMap.containsKey(fileName)) {
                    untracked.add(fileName);
                    continue;
                }
                // Judge whether is modified.
                Blob cwdBlob = new Blob(fileName, readContents(join(CWD, fileName)));
                String cwdBlobHash = sha1Hash(cwdBlob);
                if (addMap.containsKey(fileName) && cwdBlobHash.equals(addMap.get(fileName)) ||
                        !addMap.containsKey(fileName) && cwdBlobHash.equals(currentCommit.blobs.get(fileName))) {
                    modified.add(fileName);
                }
            }
        }
        // Judge whether is deleted.
        deleted.addAll(addFiles);
        commitedFiles.removeAll(rmFiles);
        deleted.addAll(commitedFiles);

        System.out.println("=== Modifications Not Staged For Commit ===");

        for (String fileName : nameList) {
            System.out.println(fileName);
        }
        System.out.println();

    }

    
}
